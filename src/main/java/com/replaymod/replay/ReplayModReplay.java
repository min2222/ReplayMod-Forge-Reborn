package com.replaymod.replay;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.Module;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.ModCompat;
import com.replaymod.core.versions.MCVer;
import com.replaymod.core.versions.MCVer.Keyboard;
import com.replaymod.replay.camera.CameraController;
import com.replaymod.replay.camera.CameraControllerRegistry;
import com.replaymod.replay.camera.CameraEntity;
import com.replaymod.replay.camera.ClassicCameraController;
import com.replaymod.replay.camera.VanillaCameraController;
import com.replaymod.replay.gui.screen.GuiModCompatWarning;
import com.replaymod.replay.handler.GuiHandler;
import com.replaymod.replay.mixin.MinecraftAccessor;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.replay.ReplayFile;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class ReplayModReplay implements Module {
	public static ReplayModReplay instance;
	private ReplayMod core;
	public KeyBindingRegistry.Binding keyPlayPause;
	private final CameraControllerRegistry cameraControllerRegistry;
	public static Logger LOGGER = LogManager.getLogger();
	private ReplayHandler replayHandler;

	public ReplayHandler getReplayHandler() {
		return this.replayHandler;
	}

	public ReplayModReplay(ReplayMod core) {
		instance = this;
		this.cameraControllerRegistry = new CameraControllerRegistry();
		this.core = core;
		core.getSettingsRegistry().register(Setting.class);
	}

	public void registerKeyBindings(KeyBindingRegistry registry) {
		registry.registerKeyBinding("replaymod.input.marker", 77, new Runnable() {
			public void run() {
				if (ReplayModReplay.this.replayHandler != null) {
					CameraEntity camera = ReplayModReplay.this.replayHandler.getCameraEntity();
					if (camera != null) {
						Marker marker = new Marker();
						marker.setTime(ReplayModReplay.this.replayHandler.getReplaySender().currentTimeStamp());
						marker.setX(camera.getX());
						marker.setY(camera.getY());
						marker.setZ(camera.getZ());
						marker.setYaw(camera.getYRot());
						marker.setPitch(camera.getXRot());
						marker.setRoll(camera.roll);
						ReplayModReplay.this.replayHandler.getOverlay().timeline.addMarker(marker);
					}
				}

			}
		}, true);
		registry.registerKeyBinding("replaymod.input.thumbnail", Keyboard.KEY_N, new Runnable() {
			@Override
			public void run() {
				if (replayHandler != null) {
					Minecraft mc = MCVer.getMinecraft();
					ListenableFuture<NoGuiScreenshot> future = NoGuiScreenshot.take(mc, 1280, 720);
					Futures.addCallback(future, new FutureCallback<NoGuiScreenshot>() {
						@Override
						public void onSuccess(NoGuiScreenshot result) {
							try {
								core.printInfoToChat("replaymod.chat.savingthumb");
								@SuppressWarnings("deprecation") // there's no easy way to produce jpg images from
																	// NativeImage
								BufferedImage image = result.getImage().toBufferedImage();
								// Encoding with alpha fails on OpenJDK and produces broken image on Sun JDK.
								BufferedImage bgrImage = new BufferedImage(image.getWidth(), image.getHeight(),
										BufferedImage.TYPE_3BYTE_BGR);
								Graphics graphics = bgrImage.getGraphics();
								graphics.drawImage(image, 0, 0, null);
								graphics.dispose();
								replayHandler.getReplayFile().writeThumb(bgrImage);
								core.printInfoToChat("replaymod.chat.savedthumb");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onFailure(Throwable t) {
							t.printStackTrace();
							core.printWarningToChat("replaymod.chat.failedthumb");
						}
					}, Runnable::run);
				}
			}
		}, true);
		this.keyPlayPause = registry.registerKeyBinding("replaymod.input.playpause", 80, new Runnable() {
			public void run() {
				if (ReplayModReplay.this.replayHandler != null) {
					ReplayModReplay.this.replayHandler.getOverlay().playPauseButton.onClick();
				}

			}
		}, true);
		this.core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.rollclockwise", 76, () -> {
		}, true);
		this.core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.rollcounterclockwise", 74, () -> {
		}, true);
		this.core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.resettilt", 75, () -> {
			Optional.ofNullable(this.replayHandler).map(ReplayHandler::getCameraEntity).ifPresent((c) -> {
				c.roll = 0.0F;
			});
		}, true);
	}

	public void initClient() {
		this.cameraControllerRegistry.register("replaymod.camera.classic",
				new Function<CameraEntity, CameraController>() {
					@Nullable
					public CameraController apply(CameraEntity cameraEntity) {
						return new ClassicCameraController(cameraEntity);
					}
				});
		this.cameraControllerRegistry.register("replaymod.camera.vanilla",
				new Function<CameraEntity, CameraController>() {
					@Nullable
					public CameraController apply(@Nullable CameraEntity cameraEntity) {
						return new VanillaCameraController(ReplayModReplay.this.core.getMinecraft(), cameraEntity);
					}
				});
		MinecraftAccessor mc = (MinecraftAccessor) this.core.getMinecraft();
		mc.setTimer(new InputReplayTimer(mc.getTimer(), this));
		(new GuiHandler(this)).register();
	}

	public void startReplay(File file) throws IOException {
		this.startReplay(this.core.files.open(file.toPath()));
	}

	public void startReplay(ReplayFile replayFile) throws IOException {
		this.startReplay(replayFile, true, true);
	}

	public ReplayHandler startReplay(ReplayFile replayFile, boolean checkModCompat, boolean asyncMode)
			throws IOException {
		if (this.replayHandler != null) {
			this.replayHandler.endReplay();
		}

		if (checkModCompat) {
			ModCompat.ModInfoDifference modDifference = new ModCompat.ModInfoDifference(replayFile.getModInfo());
			if (!modDifference.getMissing().isEmpty() || !modDifference.getDiffering().isEmpty()) {
				GuiModCompatWarning screen = new GuiModCompatWarning(modDifference);
				screen.loadButton.onClick(() -> {
					try {
						this.startReplay(replayFile, false, asyncMode);
					} catch (IOException var4) {
						var4.printStackTrace();
					}

				});
				screen.display();
				return null;
			}
		}

		this.replayHandler = new ReplayHandler(replayFile, asyncMode);
		KeyMapping.resetMapping();
		return this.replayHandler;
	}

	public void forcefullyStopReplay() {
		this.replayHandler = null;
		KeyMapping.resetMapping();
	}

	public ReplayMod getCore() {
		return this.core;
	}

	public Logger getLogger() {
		return LOGGER;
	}

	public CameraControllerRegistry getCameraControllerRegistry() {
		return this.cameraControllerRegistry;
	}

	public CameraController createCameraController(CameraEntity cameraEntity) {
		String controllerName = (String) this.core.getSettingsRegistry().get(Setting.CAMERA);
		return this.cameraControllerRegistry.create(controllerName, cameraEntity);
	}
}

package com.replaymod.render.rendering;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.pathing.player.AbstractTimelinePlayer;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.render.CameraPathExporter;
import com.replaymod.render.EXRWriter;
import com.replaymod.render.FFmpegWriter;
import com.replaymod.render.PNGWriter;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.ReplayModRender;
import com.replaymod.render.blend.BlendState;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.events.ReplayRenderCallback;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.gui.GuiRenderingDone;
import com.replaymod.render.gui.GuiVideoRenderer;
import com.replaymod.render.gui.progress.VirtualWindow;
import com.replaymod.render.hooks.ForceChunkLoadingHook;
import com.replaymod.render.metadata.MetadataInjector;
import com.replaymod.render.utils.FlawlessFrames;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.mixin.MinecraftAccessor;
import com.replaymod.replay.mixin.TimerAccessor;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.fml.ModList;

public class VideoRenderer implements RenderInfo {
	private static final ResourceLocation SOUND_RENDER_SUCCESS = new ResourceLocation("replaymod", "render_success");
	private final Minecraft mc = MCVer.getMinecraft();
	private final RenderSettings settings;
	private final ReplayHandler replayHandler;
	private final Timeline timeline;
	private final Pipeline renderingPipeline;
	private final FFmpegWriter ffmpegWriter;
	private final CameraPathExporter cameraPathExporter;
	private int fps;
	private boolean mouseWasGrabbed;
	private boolean debugInfoWasShown;
	private Map<SoundSource, Float> originalSoundLevels;
	private VideoRenderer.TimelinePlayer timelinePlayer;
	private Future<Void> timelinePlayerFuture;
	private ForceChunkLoadingHook forceChunkLoadingHook;
	private int framesDone;
	private int totalFrames;
	private final VirtualWindow guiWindow;
	private final GuiVideoRenderer gui;
	private boolean paused;
	private boolean cancelled;
	private volatile Throwable failureCause;

	public VideoRenderer(RenderSettings settings, ReplayHandler replayHandler, Timeline timeline) throws IOException {
		this.guiWindow = new VirtualWindow(this.mc);
		this.settings = settings;
		this.replayHandler = replayHandler;
		this.timeline = timeline;
		this.gui = new GuiVideoRenderer(this);
		if (settings.getRenderMethod() == RenderSettings.RenderMethod.BLEND) {
			BlendState.setState(new BlendState(settings.getOutputFile()));
			this.renderingPipeline = Pipelines.newBlendPipeline(this);
			this.ffmpegWriter = null;
		} else {
			final Object frameConsumer;
			if (settings.getEncodingPreset() == RenderSettings.EncodingPreset.EXR) {
				frameConsumer = EXRWriter.create(settings.getOutputFile().toPath(), settings.isIncludeAlphaChannel());
			} else if (settings.getEncodingPreset() == RenderSettings.EncodingPreset.PNG) {
				frameConsumer = new PNGWriter(settings.getOutputFile().toPath(), settings.isIncludeAlphaChannel());
			} else {
				frameConsumer = new FFmpegWriter(this);
			}

			this.ffmpegWriter = frameConsumer instanceof FFmpegWriter ? (FFmpegWriter) frameConsumer : null;
			FrameConsumer<BitmapFrame> previewingFrameConsumer = new FrameConsumer<BitmapFrame>() {
				private int lastFrameId = -1;

				public void consume(Map<Channel, BitmapFrame> channels) {
					BitmapFrame bgra = (BitmapFrame) channels.get(Channel.BRGA);
					if (bgra != null) {
						synchronized (this) {
							int frameId = bgra.getFrameId();
							if (this.lastFrameId < frameId) {
								this.lastFrameId = frameId;
								VideoRenderer.this.gui.updatePreview(bgra.getByteBuffer(), bgra.getSize());
							}
						}
					}

					((FrameConsumer) frameConsumer).consume(channels);
				}

				public void close() throws IOException {
					((FrameConsumer) frameConsumer).close();
				}

				public boolean isParallelCapable() {
					return ((FrameConsumer) frameConsumer).isParallelCapable();
				}
			};
			this.renderingPipeline = Pipelines.newPipeline(settings.getRenderMethod(), this, previewingFrameConsumer);
		}

		if (settings.isCameraPathExport()) {
			this.cameraPathExporter = new CameraPathExporter(settings);
		} else {
			this.cameraPathExporter = null;
		}

	}

	public boolean renderVideo() throws Throwable {
		((ReplayRenderCallback.Pre) ReplayRenderCallback.Pre.EVENT.invoker()).beforeRendering(this);
		this.setup();
		this.drawGui();
		Timer timer = ((MinecraftAccessor) this.mc).getTimer();
		Optional<Integer> optionalVideoStartTime = this.timeline.getValue(TimestampProperty.PROPERTY, 0L);
		if (optionalVideoStartTime.isPresent()) {
			int videoStart = (Integer) optionalVideoStartTime.get();
			if (videoStart > 1000) {
				int replayTime = videoStart - 1000;
				timer.partialTick = 0.0F;
				((TimerAccessor) timer).setTickLength(50.0F);

				while (replayTime < videoStart) {
					replayTime += 50;
					this.replayHandler.getReplaySender().sendPacketsTill(replayTime);
					this.tick();
				}
			}
		}

		this.renderingPipeline.run();
		if (((MinecraftAccessor) this.mc).getCrashReporter() != null) {
			throw new ReportedException((CrashReport) ((MinecraftAccessor) this.mc).getCrashReporter().get());
		} else {
			if (this.settings.isInjectSphericalMetadata()) {
				MetadataInjector.injectMetadata(this.settings.getRenderMethod(), this.settings.getOutputFile(),
						this.settings.getTargetVideoWidth(), this.settings.getTargetVideoHeight(),
						this.settings.getSphericalFovX(), this.settings.getSphericalFovY());
			}

			this.finish();
			((ReplayRenderCallback.Post) ReplayRenderCallback.Post.EVENT.invoker()).afterRendering(this);
			if (this.failureCause != null) {
				throw this.failureCause;
			} else {
				return !this.cancelled;
			}
		}
	}

	public float updateForNextFrame() {
		this.guiWindow.bind();
		if (!this.settings.isHighPerformance() || this.framesDone % this.fps == 0) {
			while (this.drawGui() && this.paused) {
				try {
					Thread.sleep(50L);
				} catch (InterruptedException var3) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}

		Timer timer = ((MinecraftAccessor) this.mc).getTimer();
		int elapsedTicks = timer.advanceTime(MCVer.milliTime());
		this.executeTaskQueue();

		while (elapsedTicks-- > 0) {
			this.tick();
		}

		this.guiWindow.unbind();
		if (this.cameraPathExporter != null) {
			this.cameraPathExporter.recordFrame(timer.partialTick);
		}

		++this.framesDone;
		return timer.partialTick;
	}

	public RenderSettings getRenderSettings() {
		return this.settings;
	}

	private void setup() {
		this.timelinePlayer = new VideoRenderer.TimelinePlayer(this.replayHandler);
		this.timelinePlayerFuture = this.timelinePlayer.start(this.timeline);
		if (this.mc.options.renderDebug) {
			this.debugInfoWasShown = true;
			this.mc.options.renderDebug = false;
		}

		if (this.mc.mouseHandler.isMouseGrabbed()) {
			this.mouseWasGrabbed = true;
		}

		this.mc.mouseHandler.releaseMouse();
		this.originalSoundLevels = new EnumMap(SoundSource.class);
		SoundSource[] var1 = SoundSource.values();
		int var2 = var1.length;

		for (int var3 = 0; var3 < var2; ++var3) {
			SoundSource category = var1[var3];
			if (category != SoundSource.MASTER) {
				this.originalSoundLevels.put(category, this.mc.options.getSoundSourceVolume(category));
				this.mc.options.setSoundCategoryVolume(category, 0.0F);
			}
		}

		this.fps = this.settings.getFramesPerSecond();
		long duration = 0L;
		Iterator var7 = this.timeline.getPaths().iterator();

		while (var7.hasNext()) {
			Path path = (Path) var7.next();
			if (path.isActive()) {
				path.updateAll();
				Collection<Keyframe> keyframes = path.getKeyframes();
				if (keyframes.size() > 0) {
					duration = Math.max(duration, ((Keyframe) Iterables.getLast(keyframes)).getTime());
				}
			}
		}

		this.totalFrames = (int) (duration * (long) this.fps / 1000L);
		if (this.cameraPathExporter != null) {
			this.cameraPathExporter.setup(this.totalFrames);
		}

		this.gui.toMinecraft().init(this.mc, this.mc.getWindow().getGuiScaledWidth(),
				this.mc.getWindow().getGuiScaledHeight());
		this.forceChunkLoadingHook = new ForceChunkLoadingHook(this.mc.levelRenderer);
	}

	private void finish() {
		if (!this.timelinePlayerFuture.isDone()) {
			this.timelinePlayerFuture.cancel(false);
		}

		this.timelinePlayer.onTick();
		this.guiWindow.close();
		this.mc.options.renderDebug = this.debugInfoWasShown;
		if (this.mouseWasGrabbed) {
			this.mc.mouseHandler.grabMouse();
		}

		Iterator<Entry<SoundSource, Float>> var1 = this.originalSoundLevels.entrySet().iterator();

		while (var1.hasNext()) {
			Entry<SoundSource, Float> entry = var1.next();
			this.mc.options.setSoundCategoryVolume(entry.getKey(), entry.getValue());
		}

		this.mc.setScreen((Screen) null);
		this.forceChunkLoadingHook.uninstall();
		if (!this.hasFailed() && this.cameraPathExporter != null) {
			try {
				this.cameraPathExporter.finish();
			} catch (IOException var4) {
				this.setFailure(var4);
			}
		}

		this.mc.getSoundManager().play(SimpleSoundInstance.forUI(new SoundEvent(SOUND_RENDER_SUCCESS), 1.0F));

		try {
			if (!this.hasFailed() && this.ffmpegWriter != null) {
				(new GuiRenderingDone(ReplayModRender.instance, this.ffmpegWriter.getVideoFile(), this.totalFrames,
						this.settings)).display();
			}
		} catch (FFmpegWriter.FFmpegStartupException var3) {
			this.setFailure(var3);
		}

		MCVer.resizeMainWindow(this.mc, this.guiWindow.getFramebufferWidth(), this.guiWindow.getFramebufferHeight());
	}

	private void executeTaskQueue() {
		while (true) {
			if (this.mc.getOverlay() != null) {
				this.drawGui();
				((MCVer.MinecraftMethodAccessor) this.mc).replayModExecuteTaskQueue();
			} else {
				CompletableFuture<Void> resourceReloadFuture = ((MinecraftAccessor) this.mc).getResourceReloadFuture();
				if (resourceReloadFuture == null) {
					((MCVer.MinecraftMethodAccessor) this.mc).replayModExecuteTaskQueue();
					this.mc.screen = this.gui.toMinecraft();
					return;
				}

				((MinecraftAccessor) this.mc).setResourceReloadFuture(null);
				this.mc.reloadResourcePacks().thenRun(() -> {
					resourceReloadFuture.complete(null);
				});
			}
		}
	}

	private void tick() {
		this.mc.tick();
	}

	public boolean drawGui() {
		Window window = this.mc.getWindow();
		if (!GLFW.glfwWindowShouldClose(window.getWindow())
				&& ((MinecraftAccessor) this.mc).getCrashReporter() == null) {
			MCVer.pushMatrix();
			RenderSystem.clear(16640, false);
			RenderSystem.enableTexture();
			this.guiWindow.beginWrite();
			RenderSystem.clear(256, Minecraft.ON_OSX);
			RenderSystem.setProjectionMatrix(
					Matrix4f.orthographic(0.0F, (float) ((double) window.getWidth() / window.getGuiScale()), 0.0F,
							(float) ((double) window.getHeight() / window.getGuiScale()), 1000.0F, 3000.0F));
			PoseStack matrixStack = RenderSystem.getModelViewStack();
			matrixStack.setIdentity();
			matrixStack.translate(0.0D, 0.0D, -2000.0D);
			RenderSystem.applyModelViewMatrix();
			Lighting.setupFor3DItems();
			this.gui.toMinecraft().init(this.mc, window.getGuiScaledWidth(), window.getGuiScaledHeight());
			int mouseX = (int) this.mc.mouseHandler.xpos() * window.getGuiScaledWidth()
					/ Math.max(window.getWidth(), 1);
			int mouseY = (int) this.mc.mouseHandler.ypos() * window.getGuiScaledHeight()
					/ Math.max(window.getHeight(), 1);
			if (this.mc.getOverlay() != null) {
				Screen orgScreen = this.mc.screen;

				try {
					this.mc.screen = this.gui.toMinecraft();
					this.mc.getOverlay().render(new PoseStack(), mouseX, mouseY, 0.0F);
				} finally {
					this.mc.screen = orgScreen;
				}
			} else {
				this.gui.toMinecraft().tick();
				this.gui.toMinecraft().render(new PoseStack(), mouseX, mouseY, 0.0F);
			}

			this.guiWindow.endWrite();
			MCVer.popMatrix();
			MCVer.pushMatrix();
			this.guiWindow.flip();
			MCVer.popMatrix();
			if (this.mc.mouseHandler.isMouseGrabbed()) {
				this.mc.mouseHandler.releaseMouse();
			}

			return !this.hasFailed() && !this.cancelled;
		} else {
			return false;
		}
	}

	public int getFramesDone() {
		return this.framesDone;
	}

	public ReadableDimension getFrameSize() {
		return new Dimension(this.settings.getVideoWidth(), this.settings.getVideoHeight());
	}

	public int getTotalFrames() {
		return this.totalFrames;
	}

	public int getVideoTime() {
		return this.framesDone * 1000 / this.fps;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isPaused() {
		return this.paused;
	}

	public void cancel() {
		if (this.ffmpegWriter != null) {
			this.ffmpegWriter.abort();
		}

		this.cancelled = true;
		this.renderingPipeline.cancel();
	}

	public boolean hasFailed() {
		return this.failureCause != null;
	}

	public synchronized void setFailure(Throwable cause) {
		if (this.failureCause != null) {
			ReplayModRender.LOGGER.error("Further failure during failed rendering: ", cause);
		} else {
			ReplayModRender.LOGGER.error("Failure during rendering: ", cause);
			this.failureCause = cause;
			this.cancel();
		}

	}

	public static String[] checkCompat(Stream<RenderSettings> settings) {
		return (String[]) settings.map(VideoRenderer::checkCompat).filter(Objects::nonNull).findFirst().orElse(null);
	}

	public static String[] checkCompat(RenderSettings settings) {
		if (ModList.get().isLoaded("sodium") && !FlawlessFrames.hasSodium()) {
			return new String[] { "Rendering is not supported with your Sodium version.",
					"It is missing support for the FREX Flawless Frames API.",
					"Either use the Sodium build from replaymod.com or uninstall Sodium before rendering!" };
		} else {
			return settings.getRenderMethod() == RenderSettings.RenderMethod.ODS && !ModList.get().isLoaded("iris")
					? new String[] { "ODS export requires Iris to be installed for Minecraft 1.17 and above.",
							"Note that it is nevertheless incompatible with other shaders and will simply replace them.",
							"Get it from: https://irisshaders.net/" }
					: null;
		}
	}

	private class TimelinePlayer extends AbstractTimelinePlayer {
		public TimelinePlayer(ReplayHandler replayHandler) {
			super(replayHandler);
		}

		public long getTimePassed() {
			return (long) VideoRenderer.this.getVideoTime();
		}
	}
}

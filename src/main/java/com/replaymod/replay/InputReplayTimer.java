package com.replaymod.replay;

import org.lwjgl.glfw.GLFW;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.ScreenExt;
import com.replaymod.replay.camera.CameraController;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;

public class InputReplayTimer extends WrappedTimer {
	private final ReplayModReplay mod;
	private final Minecraft mc;

	public InputReplayTimer(Timer wrapped, ReplayModReplay mod) {
		super(wrapped);
		this.mod = mod;
		this.mc = mod.getCore().getMinecraft();
	}

	public int advanceTime(long sysClock) {
		int ticksThisFrame = super.advanceTime(sysClock);
		ReplayMod.instance.runTasks();
		if (this.mod.getReplayHandler() != null && this.mc.level != null && this.mc.player != null) {
			if (this.mc.screen == null || ((ScreenExt) this.mc.screen).doesPassEvents()) {
				GLFW.glfwPollEvents();
				MCVer.processKeyBinds();
			}

			this.mc.keyboardHandler.tick();
			if (this.mc.screen instanceof ReceivingLevelScreen) {
				this.mc.screen.onClose();
			}
		}

		return ticksThisFrame;
	}

	public static void handleScroll(int wheel) {
		if (wheel != 0) {
			ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
			if (replayHandler != null) {
				CameraEntity cameraEntity = replayHandler.getCameraEntity();
				if (cameraEntity != null) {
					CameraController controller;
					for (controller = cameraEntity.getCameraController(); wheel > 0; --wheel) {
						controller.increaseSpeed();
					}

					while (wheel < 0) {
						controller.decreaseSpeed();
						++wheel;
					}
				}
			}
		}

	}
}

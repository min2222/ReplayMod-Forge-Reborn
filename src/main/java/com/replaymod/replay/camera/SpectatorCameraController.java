package com.replaymod.replay.camera;

import java.util.Arrays;

import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.ReplayModReplay;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class SpectatorCameraController implements CameraController {
	private final CameraEntity camera;

	public SpectatorCameraController(CameraEntity camera) {
		this.camera = camera;
	}

	public void update(float partialTicksPassed) {
		Minecraft mc = MCVer.getMinecraft();
		if (mc.options.keyShift.consumeClick()) {
			ReplayModReplay.instance.getReplayHandler().spectateCamera();
		}

		for (KeyMapping binding : Arrays.asList(mc.options.keyAttack, mc.options.keyUse, mc.options.keyJump,
				mc.options.keyShift, mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight)) {
			// noinspection StatementWithEmptyBody
			while (binding.consumeClick())
				;
		}

		Entity view = mc.getCameraEntity();
		if (view != null && view != this.camera) {
			this.camera.setCameraPosRot(mc.getCameraEntity());
		}

	}

	public void increaseSpeed() {
	}

	public void decreaseSpeed() {
	}
}

package com.replaymod.replay.camera;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class VanillaCameraController implements CameraController {
	private static final int MAX_SPEED = 2000;
	private static final int MIN_SPEED = -1000;
	private static final Vector3f[] DIRECTIONS = new Vector3f[] { new Vector3f(0.0F, 0.0F, 1.0F),
			new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(-1.0F, 0.0F, 0.0F),
			new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(0.0F, -1.0F, 0.0F) };
	private final KeyMapping[] bindings = new KeyMapping[6];
	private final CameraEntity camera;
	private int speed;

	public VanillaCameraController(Minecraft mc, CameraEntity camera) {
		this.camera = camera;
		Options gameSettings = mc.options;
		this.bindings[0] = gameSettings.keyUp;
		this.bindings[1] = gameSettings.keyDown;
		this.bindings[2] = gameSettings.keyLeft;
		this.bindings[3] = gameSettings.keyRight;
		this.bindings[4] = gameSettings.keyJump;
		this.bindings[5] = gameSettings.keyShift;
	}

	public void update(float partialTicksPassed) {
		if (partialTicksPassed != 0.0F) {
			Vector3f direction = new Vector3f(0.0F, 0.0F, 0.0F);

			for (int i = 0; i < 6; ++i) {
				if (this.bindings[i].isDown()) {
					Vector3f.add(direction, DIRECTIONS[i], direction);
				}
			}

			if (direction.length() != 0.0F) {
				direction.normalise(direction);
				double yawRadians = Math.toRadians((double) this.camera.getYRot());
				float yawSin = (float) Math.sin(yawRadians);
				float yawCos = (float) Math.cos(yawRadians);
				direction.set(direction.x * yawCos - direction.z * yawSin, direction.y,
						direction.x * yawSin + direction.z * yawCos);
				direction.scale((float) Math.pow(2.0D, (double) this.speed / 300.0D + 1.0D));
				direction.scale(partialTicksPassed / 20.0F);
				this.camera.moveCamera((double) direction.x, (double) direction.y, (double) direction.z);
			}
		}
	}

	public void increaseSpeed() {
		this.speed = Math.min(2000, this.speed + 1);
	}

	public void decreaseSpeed() {
		this.speed = Math.max(-1000, this.speed - 1);
	}
}

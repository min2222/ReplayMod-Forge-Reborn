package com.replaymod.replay.camera;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class ClassicCameraController implements CameraController {
	private static final double LOWER_SPEED = 0.2D;
	private static final double UPPER_SPEED = 200.0D;
	private static final double SPEED_CHANGE = 0.00999D;
	private final CameraEntity camera;
	private double MAX_SPEED = 10.0D;
	private double THRESHOLD;
	private double DECAY;
	private Vector3f direction;
	private Vector3f dirBefore;
	private double motion;
	private long lastCall;
	private boolean speedup;

	public ClassicCameraController(CameraEntity camera) {
		this.THRESHOLD = this.MAX_SPEED / 20.0D;
		this.DECAY = this.MAX_SPEED / 3.0D;
		this.lastCall = System.currentTimeMillis();
		this.speedup = false;
		this.camera = camera;
	}

	public void update(float partialTicksPassed) {
		boolean forward = false;
		boolean backward = false;
		boolean left = false;
		boolean right = false;
		boolean up = false;
		boolean down = false;
		this.speedup = false;
		KeyMapping[] var8 = Minecraft.getInstance().options.keyMappings;
		int var9 = var8.length;

		for (int var10 = 0; var10 < var9; ++var10) {
			KeyMapping kb = var8[var10];
			if (kb.isDown()) {
				if (kb.getName().equals("key.forward")) {
					forward = true;
					this.speedup = true;
				}

				if (kb.getName().equals("key.back")) {
					backward = true;
					this.speedup = true;
				}

				if (kb.getName().equals("key.jump")) {
					up = true;
					this.speedup = true;
				}

				if (kb.getName().equals("key.left")) {
					left = true;
					this.speedup = true;
				}

				if (kb.getName().equals("key.right")) {
					right = true;
					this.speedup = true;
				}

				if (kb.getName().equals("key.sneak")) {
					down = true;
					this.speedup = true;
				}
			}
		}

		this.forwardCameraMovement(forward, backward, left, right, up, down);
		this.updateMovement();
	}

	public void increaseSpeed() {
		this.setCameraMaximumSpeed(this.MAX_SPEED + 0.00999D);
	}

	public void decreaseSpeed() {
		this.setCameraMaximumSpeed(this.MAX_SPEED - 0.00999D);
	}

	private void setCameraMaximumSpeed(double maxSpeed) {
		if (!(maxSpeed < 0.2D) && !(maxSpeed > 200.0D)) {
			this.MAX_SPEED = maxSpeed;
			this.THRESHOLD = this.MAX_SPEED / 20.0D;
			this.DECAY = 5.0D;
		}
	}

	private void forwardCameraMovement(boolean forward, boolean backward, boolean left, boolean right, boolean up,
			boolean down) {
		if (forward && !backward) {
			this.setMovement(ClassicCameraController.MoveDirection.FORWARD);
		} else if (backward && !forward) {
			this.setMovement(ClassicCameraController.MoveDirection.BACKWARD);
		}

		if (left && !right) {
			this.setMovement(ClassicCameraController.MoveDirection.LEFT);
		} else if (right && !left) {
			this.setMovement(ClassicCameraController.MoveDirection.RIGHT);
		}

		if (up && !down) {
			this.setMovement(ClassicCameraController.MoveDirection.UP);
		} else if (down && !up) {
			this.setMovement(ClassicCameraController.MoveDirection.DOWN);
		}

	}

	private void updateMovement() {
		long frac = System.currentTimeMillis() - this.lastCall;
		if (frac != 0L) {
			double decFac = Math.max(0.0D, 1.0D - this.DECAY * ((double) frac / 1000.0D));
			if (this.speedup) {
				if (this.motion < this.THRESHOLD) {
					this.motion = this.THRESHOLD;
				}

				this.motion /= decFac;
			} else {
				this.motion *= decFac;
			}

			this.motion = Math.min(this.motion, this.MAX_SPEED);
			this.lastCall = System.currentTimeMillis();
			if (this.direction != null && this.direction.lengthSquared() != 0.0F && !(this.motion < this.THRESHOLD)) {
				Vector3f movement = this.direction.normalise((Vector3f) null);
				double factor = this.motion * ((double) frac / 1000.0D);
				this.camera.moveCamera((double) movement.x * factor, (double) movement.y * factor,
						(double) movement.z * factor);
			}
		}
	}

	private void setMovement(ClassicCameraController.MoveDirection dir) {
		float rotationPitch = this.camera.getXRot();
		float rotationYaw = this.camera.getYRot();
		switch (dir) {
		case BACKWARD:
			this.direction = this.getVectorForRotation(-rotationPitch, rotationYaw - 180.0F);
			break;
		case DOWN:
			this.direction = this.getVectorForRotation(90.0F, 0.0F);
			break;
		case FORWARD:
			this.direction = this.getVectorForRotation(rotationPitch, rotationYaw);
			break;
		case LEFT:
			this.direction = this.getVectorForRotation(0.0F, rotationYaw - 90.0F);
			break;
		case RIGHT:
			this.direction = this.getVectorForRotation(0.0F, rotationYaw + 90.0F);
			break;
		case UP:
			this.direction = this.getVectorForRotation(-90.0F, 0.0F);
		}

		Vector3f dbf = this.direction;
		if (this.dirBefore != null) {
			this.dirBefore.normalise(this.dirBefore);
			Vector3f.add(this.direction, this.dirBefore, this.dirBefore);
			this.direction = this.dirBefore;
		}

		this.dirBefore = dbf;
		this.updateMovement();
	}

	private Vector3f getVectorForRotation(float pitch, float yaw) {
		float f2 = Mth.cos(-yaw * 0.017453292F - 3.1415927F);
		float f3 = Mth.sin(-yaw * 0.017453292F - 3.1415927F);
		float f4 = -Mth.cos(-pitch * 0.017453292F);
		float f5 = Mth.sin(-pitch * 0.017453292F);
		return new Vector3f(f3 * f4, f5, f2 * f4);
	}

	public enum MoveDirection {
		UP, DOWN, LEFT, RIGHT, FORWARD, BACKWARD
	}
}

package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.versions.MCVer;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.hooks.EntityRendererHandler;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;

@Mixin({ Camera.class })
public abstract class Mixin_StabilizeCamera {
	private float orgYaw;
	private float orgPitch;
	private float orgPrevYaw;
	private float orgPrevPitch;
	private float orgRoll;
	private float orgHeadYaw;
	private float orgPrevHeadYaw;

	private EntityRendererHandler getHandler() {
		return ((EntityRendererHandler.IEntityRenderer) MCVer.getMinecraft().gameRenderer).replayModRender_getHandler();
	}

	@Inject(method = { "setup" }, at = { @At("HEAD") })
	private void replayModRender_beforeSetupCameraTransform(BlockGetter blockView, Entity entity, boolean thirdPerson,
			boolean inverseView, float partialTicks, CallbackInfo ci) {
		if (this.getHandler() != null) {
			this.orgYaw = entity.getYRot();
			this.orgPitch = entity.getXRot();
			this.orgPrevYaw = entity.yRotO;
			this.orgPrevPitch = entity.xRotO;
			this.orgRoll = entity instanceof CameraEntity ? ((CameraEntity) entity).roll : 0.0F;
			if (entity instanceof LivingEntity) {
				this.orgHeadYaw = ((LivingEntity) entity).yHeadRot;
				this.orgPrevHeadYaw = ((LivingEntity) entity).yHeadRotO;
			}
		}

		if (this.getHandler() != null) {
			RenderSettings settings = this.getHandler().getSettings();
			if (settings.isStabilizeYaw()) {
				entity.yRotO = 0.0F;
				entity.setYRot(0.0F);
				if (entity instanceof LivingEntity) {
					((LivingEntity) entity).yHeadRotO = ((LivingEntity) entity).yHeadRot = 0.0F;
				}
			}

			if (settings.isStabilizePitch()) {
				entity.xRotO = 0.0F;
				entity.setXRot(0.0F);
			}

			if (settings.isStabilizeRoll() && entity instanceof CameraEntity) {
				((CameraEntity) entity).roll = 0.0F;
			}
		}

	}

	@Inject(method = { "setup" }, at = { @At("RETURN") })
	private void replayModRender_afterSetupCameraTransform(BlockGetter blockView, Entity entity, boolean thirdPerson,
			boolean inverseView, float partialTicks, CallbackInfo ci) {
		if (this.getHandler() != null) {
			entity.setYRot(this.orgYaw);
			entity.setXRot(this.orgPitch);
			entity.yRotO = this.orgPrevYaw;
			entity.xRotO = this.orgPrevPitch;
			if (entity instanceof CameraEntity) {
				((CameraEntity) entity).roll = this.orgRoll;
			}

			if (entity instanceof LivingEntity) {
				((LivingEntity) entity).yHeadRot = this.orgHeadYaw;
				((LivingEntity) entity).yHeadRotO = this.orgPrevHeadYaw;
			}
		}

	}
}

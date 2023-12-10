package com.replaymod.replay.mixin.entity_tracking;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.replay.ext.EntityExt;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;

@Mixin(ClientPacketListener.class)
public class Mixin_FixPartialUpdates {

	@Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getYaw()F", opcode = Opcodes.GETFIELD))
	private float getTrackedYaw(Entity instance) {
		return ((EntityExt) instance).replaymod$getTrackedYaw();
	}

	@Redirect(method = "handleMoveEntity", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;getXRot()F", opcode = Opcodes.GETFIELD))
	private float getTrackedPitch(Entity instance) {
		return ((EntityExt) instance).replaymod$getTrackedPitch();
	}

	@Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D"))
	private double getTrackedX(Entity instance) {
		return instance.getPositionCodec().decode(0, 0, 0).x();
	}

	@Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D"))
	private double getTrackedY(Entity instance) {
		return instance.getPositionCodec().decode(0, 0, 0).y();
	}

	@Redirect(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D"))
	private double getTrackedZ(Entity instance) {
		return instance.getPositionCodec().decode(0, 0, 0).z();
	}

	private static final String ENTITY_UPDATE = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFIZ)V";

	@Unique
	private Entity entity;

	@ModifyVariable(method = { "handleMoveEntity",
			"handleTeleportEntity" }, at = @At(value = "INVOKE", target = ENTITY_UPDATE), ordinal = 0)
	private Entity captureEntity(Entity entity) {
		return this.entity = entity;
	}

	@Inject(method = { "handleMoveEntity", "handleTeleportEntity" }, at = @At("RETURN"))
	private void resetEntityField(CallbackInfo ci) {
		this.entity = null;
	}

	@ModifyArg(method = { "handleMoveEntity",
			"handleTeleportEntity" }, at = @At(value = "INVOKE", target = ENTITY_UPDATE), index = 3)
	private float captureTrackedYaw(float value) {
		((EntityExt) this.entity).replaymod$setTrackedYaw(value);
		return value;
	}

	@ModifyArg(method = { "handleMoveEntity",
			"handleTeleportEntity" }, at = @At(value = "INVOKE", target = ENTITY_UPDATE), index = 4)
	private float captureTrackedPitch(float value) {
		((EntityExt) this.entity).replaymod$setTrackedPitch(value);
		return value;
	}
}

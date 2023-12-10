package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.renderer.GameRenderer;

@Mixin({ GameRenderer.class })
public abstract class Mixin_Omnidirectional_Camera implements EntityRendererHandler.IEntityRenderer {
	private static final String METHOD = "getBasicProjectionMatrix";
	private static final String TARGET = "Lnet/minecraft/util/math/Matrix4f;viewboxMatrix(DFFF)Lnet/minecraft/util/math/Matrix4f;";
	private static final boolean TARGET_REMAP = true;
	private static final float OMNIDIRECTIONAL_FOV = 90.0F;

	@ModifyArg(method = {
			"getProjectionMatrix" }, at = @At(value = "INVOKE", target = "Lcom/mojang/math/Matrix4f;perspective(DFFF)Lcom/mojang/math/Matrix4f;", remap = true), index = 0)
	private double replayModRender_perspective_fov(double fovY) {
		return this.isOmnidirectional() ? 90.0D : fovY;
	}

	@ModifyArg(method = {
			"getProjectionMatrix" }, at = @At(value = "INVOKE", target = "Lcom/mojang/math/Matrix4f;perspective(DFFF)Lcom/mojang/math/Matrix4f;", remap = true), index = 1)
	private float replayModRender_perspective_aspect(float aspect) {
		return this.isOmnidirectional() ? 1.0F : aspect;
	}

	@Unique
	private boolean isOmnidirectional() {
		return this.replayModRender_getHandler() != null && this.replayModRender_getHandler().omnidirectional;
	}
}

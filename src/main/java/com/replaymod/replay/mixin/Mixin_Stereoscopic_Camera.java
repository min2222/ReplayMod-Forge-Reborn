package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.replaymod.render.capturer.StereoscopicOpenGlFrameCapturer;
import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.renderer.GameRenderer;

@Mixin({ GameRenderer.class })
public abstract class Mixin_Stereoscopic_Camera implements EntityRendererHandler.IEntityRenderer {
	@Inject(method = { "getProjectionMatrix" }, at = { @At("RETURN") }, cancellable = true)
	private void replayModRender_setupStereoscopicProjection(CallbackInfoReturnable<Matrix4f> ci) {
		if (this.replayModRender_getHandler() != null) {
			Matrix4f offset;
			if (this.replayModRender_getHandler().data == StereoscopicOpenGlFrameCapturer.Data.LEFT_EYE) {
				offset = Matrix4f.createTranslateMatrix(0.07F, 0.0F, 0.0F);
			} else {
				if (this.replayModRender_getHandler().data != StereoscopicOpenGlFrameCapturer.Data.RIGHT_EYE) {
					return;
				}

				offset = Matrix4f.createTranslateMatrix(-0.07F, 0.0F, 0.0F);
			}

			offset.multiply((Matrix4f) ci.getReturnValue());
			ci.setReturnValue(offset);
		}

	}

	@Inject(method = { "renderLevel" }, at = { @At("HEAD") })
	private void replayModRender_setupStereoscopicProjection(float partialTicks, long frameStartNano,
			PoseStack matrixStack, CallbackInfo ci) {
		if (this.replayModRender_getHandler() != null) {
			if (this.replayModRender_getHandler().data == StereoscopicOpenGlFrameCapturer.Data.LEFT_EYE) {
				matrixStack.translate(0.1D, 0.0D, 0.0D);
			} else if (this.replayModRender_getHandler().data == StereoscopicOpenGlFrameCapturer.Data.RIGHT_EYE) {
				matrixStack.translate(-0.1D, 0.0D, 0.0D);
			}
		}

	}
}

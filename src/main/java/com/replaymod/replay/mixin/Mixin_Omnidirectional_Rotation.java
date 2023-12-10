package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.replaymod.core.versions.MCVer;
import com.replaymod.render.capturer.CubicOpenGlFrameCapturer;
import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.renderer.GameRenderer;

@Mixin({ GameRenderer.class })
public abstract class Mixin_Omnidirectional_Rotation {
	private EntityRendererHandler getHandler() {
		return ((EntityRendererHandler.IEntityRenderer) MCVer.getMinecraft().gameRenderer).replayModRender_getHandler();
	}

	@Inject(method = { "renderLevel" }, at = { @At("HEAD") })
	private void replayModRender_setupCubicFrameRotation(float partialTicks, long frameStartNano, PoseStack matrixStack,
			CallbackInfo ci) {
		if (this.getHandler() != null && this.getHandler().data instanceof CubicOpenGlFrameCapturer.Data) {
			CubicOpenGlFrameCapturer.Data data = (CubicOpenGlFrameCapturer.Data) this.getHandler().data;
			float angle = 0.0F;
			float x = 0.0F;
			float y = 0.0F;
			switch (data) {
			case FRONT:
				angle = 0.0F;
				y = 1.0F;
				break;
			case RIGHT:
				angle = 90.0F;
				y = 1.0F;
				break;
			case BACK:
				angle = 180.0F;
				y = 1.0F;
				break;
			case LEFT:
				angle = -90.0F;
				y = 1.0F;
				break;
			case TOP:
				angle = -90.0F;
				x = 1.0F;
				break;
			case BOTTOM:
				angle = 90.0F;
				x = 1.0F;
			}

			matrixStack.mulPose((new Vector3f(x, y, 0.0F)).rotationDegrees(angle));
			MCVer.getMinecraft().levelRenderer.needsUpdate();
		}

	}
}

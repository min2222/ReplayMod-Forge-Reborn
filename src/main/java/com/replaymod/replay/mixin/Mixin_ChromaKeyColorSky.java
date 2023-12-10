package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin({ LevelRenderer.class })
public abstract class Mixin_ChromaKeyColorSky {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = {
			"renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V" }, at = {
					@At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", remap = false, shift = Shift.AFTER) }, cancellable = true)
	private void chromaKeyingSky(CallbackInfo ci) {
		EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) this.minecraft.gameRenderer)
				.replayModRender_getHandler();
		if (handler != null) {
			ReadableColor color = handler.getSettings().getChromaKeyingColor();
			if (color != null) {
				RenderSystem.clearColor((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F,
						(float) color.getBlue() / 255.0F, 1.0F);
				RenderSystem.clear(16384, false);
				ci.cancel();
			}
		}

	}
}

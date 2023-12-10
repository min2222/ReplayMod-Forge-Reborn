package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.core.events.PreRenderHandCallback;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

@Pseudo
@Mixin(targets = { "shadersmod/client/ShadersRender", "net/optifine/shaders/ShadersRender" }, remap = false)
public abstract class MixinShadersRender {
	@Inject(method = { "renderHand0", "renderHand1" }, at = { @At("HEAD") }, cancellable = true, remap = false)
	private static void replayModCompat_disableRenderHand0(GameRenderer er, PoseStack stack, Camera camera,
			float partialTicks, CallbackInfo ci) {
		if (((PreRenderHandCallback) PreRenderHandCallback.EVENT.invoker()).preRenderHand()) {
			ci.cancel();
		}

	}
}

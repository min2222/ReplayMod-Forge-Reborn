package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PostRenderScreenCallback;

import net.minecraft.client.renderer.GameRenderer;

@Mixin({ GameRenderer.class })
public class MixinGameRenderer {
	@Unique
	private PoseStack context;

	@ModifyArg(method = {
			"render" }, at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"))
	private PoseStack captureContext(PoseStack context) {
		this.context = context;
		return context;
	}

	@Inject(method = { "render" }, at = {
			@At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", shift = Shift.AFTER) })
	private void postRenderScreen(float partialTicks, long nanoTime, boolean renderWorld, CallbackInfo ci) {
		((PostRenderScreenCallback) PostRenderScreenCallback.EVENT.invoker()).postRenderScreen(this.context,
				partialTicks);
	}
}

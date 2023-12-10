package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.RenderHudCallback;

import net.minecraft.client.gui.Gui;

@Mixin({ Gui.class })
public class Mixin_RenderHudCallback {
	@Inject(method = { "render" }, at = {
			@At(value = "FIELD", opcode = 180, target = "Lnet/minecraft/client/Options;renderDebug:Z") })
	private void renderOverlay(PoseStack stack, float partialTicks, CallbackInfo ci) {
		((RenderHudCallback) RenderHudCallback.EVENT.invoker()).renderHud(stack, partialTicks);
	}
}

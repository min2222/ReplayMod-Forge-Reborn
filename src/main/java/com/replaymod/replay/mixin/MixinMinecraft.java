package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.OpenGuiScreenCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Mixin({ Minecraft.class })
public abstract class MixinMinecraft {
	@Inject(method = { "tick" }, at = { @At("HEAD") })
	private void preTick(CallbackInfo ci) {
		((PreTickCallback) PreTickCallback.EVENT.invoker()).preTick();
	}

	@Inject(method = { "setScreen" }, at = {
			@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;") })
	private void openGuiScreen(Screen newGuiScreen, CallbackInfo ci) {
		((OpenGuiScreenCallback) OpenGuiScreenCallback.EVENT.invoker()).openGuiScreen(newGuiScreen);
	}
}

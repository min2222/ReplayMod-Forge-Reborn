package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.events.KeyBindingEventCallback;

import net.minecraft.client.MouseHandler;

@Mixin({ MouseHandler.class })
public class MixinMouse {
	@Inject(method = { "onPress" }, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", shift = Shift.AFTER) })
	private void afterKeyBindingTick(CallbackInfo ci) {
		((KeyBindingEventCallback) KeyBindingEventCallback.EVENT.invoker()).onKeybindingEvent();
	}
}

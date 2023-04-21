package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.KeyEventCallback;

import net.minecraft.client.KeyboardHandler;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardListener {
    private static final String ON_KEY_PRESSED = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V";

    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = ON_KEY_PRESSED), cancellable = true)
    private void beforeKeyMappingTick(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (KeyEventCallback.EVENT.invoker().onKeyEvent(key, scanCode, action, modifiers)) {
            ci.cancel();
        }
    }

    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = ON_KEY_PRESSED, shift = At.Shift.AFTER))
    private void afterKeyMappingTick(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        KeyBindingEventCallback.EVENT.invoker().onKeybindingEvent();
    }
}

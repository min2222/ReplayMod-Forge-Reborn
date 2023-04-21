package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.mojang.blaze3d.platform.GlStateManager;

@Mixin(GlStateManager.class)
public abstract class MixinGlStateManager {
	//1.16.5 and below
    /*@Shadow
    private static int activeTexture;

    @Inject(method = "enableFog", at = @At("HEAD"))
    private static void enableFog(CallbackInfo ci) {
        FogStateCallback.EVENT.invoker().fogStateChanged(true);
    }

    @Inject(method = "disableFog", at = @At("HEAD"))
    private static void disableFog(CallbackInfo ci) {
        FogStateCallback.EVENT.invoker().fogStateChanged(false);
    }

    @Inject(method = "_enableTexture", at = @At("HEAD"))
    private static void enableTexture(CallbackInfo ci) {
        Texture2DStateCallback.EVENT.invoker().texture2DStateChanged(MixinGlStateManager.activeTexture, true);
    }

    @Inject(method = "_disableTexture", at = @At("HEAD"))
    private static void disableTexture(CallbackInfo ci) {
        Texture2DStateCallback.EVENT.invoker().texture2DStateChanged(MixinGlStateManager.activeTexture, false);
    }*/
}

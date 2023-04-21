package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.render.hooks.EntityRendererHandler;

import de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

/**
 * Instead of rendering the normal sky, clears the screen with a uniform color for use with chroma keying.
 */
@Mixin(LevelRenderer.class)
public abstract class Mixin_ChromaKeyColorSky {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private void chromaKeyingSky(CallbackInfo ci) {
        EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) this.minecraft.gameRenderer).replayModRender_getHandler();
        if (handler != null) {
            ReadableColor color = handler.getSettings().getChromaKeyingColor();
            if (color != null) {
            	RenderSystem.clearColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);
                RenderSystem.clear(16384, false);
                ci.cancel();
            }
        }
    }
}

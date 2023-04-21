package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.replaymod.render.hooks.EntityRendererHandler;

import de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

/**
 * Forces the sky to always render when chroma keying is active. Ordinarily it only renders when the render distance is
 * at 4 or greater.
 */
@Mixin(LevelRenderer.class)
public abstract class Mixin_ChromaKeyForceSky {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyConstant(method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V", constant = @Constant(intValue = 4))
    private int forceSkyWhenChromaKeying(int value) {
        EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) this.minecraft.gameRenderer).replayModRender_getHandler();
        if (handler != null) {
            ReadableColor color = handler.getSettings().getChromaKeyingColor();
            if (color != null) {
                return 0;
            }
        }
        return value;
    }
}

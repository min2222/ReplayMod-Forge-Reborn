package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinRenderLivingBase {
}

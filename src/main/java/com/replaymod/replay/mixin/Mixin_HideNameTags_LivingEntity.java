package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;

@Mixin({ LivingEntityRenderer.class })
public abstract class Mixin_HideNameTags_LivingEntity {
}

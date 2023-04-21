package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.entity.ItemRenderer;

@Mixin(ItemRenderer.class)
public abstract class MixinRenderItem {
}

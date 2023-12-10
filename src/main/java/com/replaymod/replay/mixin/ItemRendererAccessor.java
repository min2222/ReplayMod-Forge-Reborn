package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.entity.ItemRenderer;

@Mixin({ ItemRenderer.class })
public interface ItemRendererAccessor {
	@Accessor("itemColors")
	ItemColors getItemColors();
}

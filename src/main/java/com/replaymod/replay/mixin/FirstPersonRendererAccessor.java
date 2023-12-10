package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

@Mixin({ ItemInHandRenderer.class })
public interface FirstPersonRendererAccessor {
	@Accessor("mainHandItem")
	void setItemStackMainHand(ItemStack itemStack);

	@Accessor("offHandItem")
	void setItemStackOffHand(ItemStack itemStack);

	@Accessor("mainHandHeight")
	void setEquippedProgressMainHand(float f);

	@Accessor("oMainHandHeight")
	void setPrevEquippedProgressMainHand(float f);

	@Accessor("offHandHeight")
	void setEquippedProgressOffHand(float f);

	@Accessor("oOffHandHeight")
	void setPrevEquippedProgressOffHand(float f);
}

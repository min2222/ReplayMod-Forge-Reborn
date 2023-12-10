package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin({ Player.class })
public interface EntityPlayerAccessor extends EntityLivingBaseAccessor {
	@Accessor("lastItemInMainHand")
	ItemStack getItemStackMainHand();

	@Accessor("lastItemInMainHand")
	void setItemStackMainHand(ItemStack itemStack);
}

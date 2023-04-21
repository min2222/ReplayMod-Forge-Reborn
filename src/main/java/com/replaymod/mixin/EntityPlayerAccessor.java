package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Player.class)
public interface EntityPlayerAccessor extends Mixin_EntityLivingBaseAccessor {
    @Accessor("lastItemInMainHand")
    ItemStack getMainHandItem();

    @Accessor("lastItemInMainHand")
    void setItemInMainHand(ItemStack value);
}

package com.replaymod.mixin;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface EntityLivingBaseAccessor {
    @Accessor("DATA_LIVING_ENTITY_FLAGS")
    @Nonnull
    static EntityDataAccessor<Byte> getLivingFlags() {
        return null;
    }
}

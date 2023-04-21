package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface Mixin_EntityLivingBaseAccessor {
    @Accessor("lerpX")
    double getInterpTargetX();

    @Accessor("lerpY")
    double getInterpTargetY();

    @Accessor("lerpZ")
    double getInterpTargetZ();

    @Accessor("lerpYRot")
    double getInterpTargetYaw();

    @Accessor("lerpXRot")
    double getInterpTargetPitch();

    @Accessor("useItemRemaining")
    int getActiveItemStackUseCount();

    @Accessor("useItemRemaining")
    void setActiveItemStackUseCount(int value);
}

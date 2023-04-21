package com.replaymod.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor("xo")
    double getPrevPosX();

    @Accessor("yo")
    double getPrevPosY();

    @Accessor("zo")
    double getPrevPosZ();

    @Accessor("x")
    double getPosX();

    @Accessor("y")
    double getPosY();

    @Accessor("z")
    double getPosZ();
}

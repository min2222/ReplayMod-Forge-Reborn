package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.particle.Particle;

@Mixin({ Particle.class })
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

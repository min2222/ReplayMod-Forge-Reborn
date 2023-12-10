package com.replaymod.replay.mixin;

import java.util.Queue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;

@Mixin({ ParticleEngine.class })
public abstract class MixinParticleManager {
	@Final
	@Shadow
	private Queue<Particle> particlesToAdd;

	@Inject(method = { "setLevel" }, at = { @At("HEAD") })
	public void replayModReplay_clearParticleQueue(ClientLevel world, CallbackInfo ci) {
		this.particlesToAdd.clear();
	}
}

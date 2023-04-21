package com.replaymod.mixin;

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

@Mixin(ParticleEngine.class)
public abstract class Mixin_ParticleManager {
    @Final
    @Shadow
    private Queue<Particle> particlesToAdd;

    /**
     * This method additionally clears the queue of particles to be added when the world is changed.
     * Otherwise particles from the previous world might show up in this one if they were spawned after
     * the last tick in the previous world.
     *
     * @param world The new world
     * @param ci    Callback info
     */
    @Inject(method = "setLevel", at = @At("HEAD"))
    public void replayModReplay_clearParticleQueue(
            ClientLevel world,
            CallbackInfo ci) {
        this.particlesToAdd.clear();
    }
}

package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.ClientLevel;

@Mixin({ ClientLevel.class })
public abstract class MixinWorldClient {
}

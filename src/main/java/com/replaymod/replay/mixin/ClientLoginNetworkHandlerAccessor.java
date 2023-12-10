package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;

@Mixin({ ClientHandshakePacketListenerImpl.class })
public interface ClientLoginNetworkHandlerAccessor {
}

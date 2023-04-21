package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;

@Mixin(ClientboundAddPlayerPacket.class)
public interface SPacketSpawnPlayerAccessor {
}

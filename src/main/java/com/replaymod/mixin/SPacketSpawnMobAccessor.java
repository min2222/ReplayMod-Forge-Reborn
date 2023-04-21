package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;

@Mixin(ClientboundAddEntityPacket.class)
public interface SPacketSpawnMobAccessor {
}

package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.server.IntegratedServer;

@Mixin({ IntegratedServer.class })
public interface IntegratedServerAccessor {
	@Accessor("paused")
	boolean isGamePaused();
}

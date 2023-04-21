package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.server.IntegratedServer;

@Mixin(IntegratedServer.class)
public interface IntegratedServerAccessor {
    // TODO probably https://github.com/ReplayMod/remap/issues/10
    @Accessor("paused")
    boolean isGamePaused();
}

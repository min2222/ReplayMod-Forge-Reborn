package com.replaymod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;

@Mixin(NetworkRegistry.class)
public interface NetworkRegistryAccessor {
    @Invoker("gatherLoginPayloads")
    static List<NetworkRegistry.LoginPayload> invokeGatherLoginPayloads(NetworkDirection direction, boolean isLocal) {
        throw new AssertionError();
    }
}

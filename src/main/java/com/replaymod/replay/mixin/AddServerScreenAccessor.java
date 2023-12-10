package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.multiplayer.ServerData;

@Mixin({ EditServerScreen.class })
public interface AddServerScreenAccessor {
	@Accessor("serverData")
	ServerData getServer();
}

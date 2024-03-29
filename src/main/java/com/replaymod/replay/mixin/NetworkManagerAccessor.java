package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;

@Mixin({ Connection.class })
public interface NetworkManagerAccessor {
	@Accessor("channel")
	Channel getChannel();
}

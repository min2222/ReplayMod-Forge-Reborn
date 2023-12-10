package com.replaymod.replay.mixin;

import java.util.Iterator;
import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.minecraft.network.Connection;

@Mixin({ Connection.class })
public abstract class MixinClientConnection {
	@Shadow
	private Channel channel;

	@Inject(method = { "setupCompression" }, at = { @At("RETURN") })
	private void ensureReplayModRecorderIsAfterDecompress(CallbackInfo ci) {
		ChannelHandler recorder = null;
		Iterator var3 = this.channel.pipeline().iterator();

		String key;
		do {
			if (!var3.hasNext()) {
				return;
			}

			Entry<String, ChannelHandler> entry = (Entry) var3.next();
			key = (String) entry.getKey();
			if ("replay_recorder_raw".equals(key)) {
				recorder = (ChannelHandler) entry.getValue();
			}
		} while (!"decompress".equals(key) || recorder == null);

		this.channel.pipeline().remove(recorder);
		this.channel.pipeline().addBefore("decoder", "replay_recorder_raw", recorder);
	}
}

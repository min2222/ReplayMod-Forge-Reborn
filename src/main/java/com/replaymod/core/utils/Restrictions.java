package com.replaymod.core.utils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

public class Restrictions {
	public static final ResourceLocation PLUGIN_CHANNEL = new ResourceLocation("replaymod", "restrict");
	private boolean noXray;
	private boolean noNoclip;
	private boolean onlyFirstPerson;
	private boolean onlyRecordingPlayer;

	public String handle(ClientboundCustomPayloadPacket packet) {
		FriendlyByteBuf buffer = packet.getData();
		if (buffer.isReadable()) {
			String name = buffer.readUtf(64);
			boolean active = buffer.readBoolean();
			return name;
		} else {
			return null;
		}
	}

	public boolean isNoXray() {
		return this.noXray;
	}

	public boolean isNoNoclip() {
		return this.noNoclip;
	}

	public boolean isOnlyFirstPerson() {
		return this.onlyFirstPerson;
	}

	public boolean isOnlyRecordingPlayer() {
		return this.onlyRecordingPlayer;
	}
}

package com.replaymod.recording;

import net.minecraft.client.multiplayer.ServerData;

public interface ServerInfoExt {
	static ServerInfoExt from(ServerData base) {
		return (ServerInfoExt) base;
	}

	Boolean getAutoRecording();

	void setAutoRecording(Boolean boolean_);
}

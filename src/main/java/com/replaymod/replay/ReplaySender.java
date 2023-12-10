package com.replaymod.replay;

import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.mixin.MinecraftAccessor;
import com.replaymod.replay.mixin.TimerAccessor;

import net.minecraft.client.Minecraft;

public interface ReplaySender {
	int currentTimeStamp();

	default boolean paused() {
		Minecraft mc = MCVer.getMinecraft();
		TimerAccessor timer = (TimerAccessor) ((MinecraftAccessor) mc).getTimer();
		return timer.getTickLength() == Float.POSITIVE_INFINITY;
	}

	void setReplaySpeed(double d);

	double getReplaySpeed();

	boolean isAsyncMode();

	void setAsyncMode(boolean bl);

	void setSyncModeAndWait();

	void jumpToTime(int i);

	void sendPacketsTill(int i);
}

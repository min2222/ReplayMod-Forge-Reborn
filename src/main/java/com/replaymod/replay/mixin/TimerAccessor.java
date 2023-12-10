package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Timer;

@Mixin({ Timer.class })
public interface TimerAccessor {
	@Accessor("lastMs")
	long getLastSyncSysClock();

	@Accessor("lastMs")
	void setLastSyncSysClock(long l);

	@Accessor("msPerTick")
	float getTickLength();

	@Accessor("msPerTick")
	@Mutable
	void setTickLength(float f);
}

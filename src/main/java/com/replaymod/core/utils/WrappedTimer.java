package com.replaymod.core.utils;

import com.replaymod.replay.mixin.TimerAccessor;

import net.minecraft.client.Timer;

public class WrappedTimer extends Timer {
	public static final float DEFAULT_MS_PER_TICK = 1000 / 20;

	protected final Timer wrapped;

	public WrappedTimer(Timer wrapped) {
		super(0, 0);
		this.wrapped = wrapped;
		copy(wrapped, this);
	}

	@Override
	public int advanceTime(long sysClock) {
		copy(this, wrapped);
		try {
			return wrapped.advanceTime(sysClock);
		} finally {
			copy(wrapped, this);
		}
	}

	protected void copy(Timer from, Timer to) {
		TimerAccessor fromA = (TimerAccessor) from;
		TimerAccessor toA = (TimerAccessor) to;

		to.partialTick = from.partialTick;
		toA.setLastSyncSysClock(fromA.getLastSyncSysClock());
		to.tickDelta = from.tickDelta;
		toA.setTickLength(fromA.getTickLength());
	}
}

package com.replaymod.pathing.player;

import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

import net.minecraft.client.Timer;

public class ReplayTimer extends WrappedTimer {
	private final Timer state = new Timer(0.0F, 0L);
	public int ticksThisFrame;

	public ReplayTimer(Timer wrapped) {
		super(wrapped);
	}

	public int advanceTime(long sysClock) {
		this.copy(this, this.state);

		try {
			this.ticksThisFrame = this.wrapped.advanceTime(sysClock);
		} finally {
			this.copy(this.state, this);
			((ReplayTimer.UpdatedCallback) ReplayTimer.UpdatedCallback.EVENT.invoker()).onUpdate();
		}

		return this.ticksThisFrame;
	}

	public Timer getWrapped() {
		return this.wrapped;
	}

	public interface UpdatedCallback {
		Event<UpdatedCallback> EVENT = Event.create((listeners) -> () -> {
			for (UpdatedCallback listener : listeners) {
				listener.onUpdate();
			}
		});

		void onUpdate();
	}
}

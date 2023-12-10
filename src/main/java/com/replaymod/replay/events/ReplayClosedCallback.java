package com.replaymod.replay.events;

import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.replay.ReplayHandler;

public interface ReplayClosedCallback {
	Event<ReplayClosedCallback> EVENT = Event.create((listeners) -> {
		return (replayHandler) -> {
			Iterator var2 = listeners.iterator();

			while (var2.hasNext()) {
				ReplayClosedCallback listener = (ReplayClosedCallback) var2.next();
				listener.replayClosed(replayHandler);
			}

		};
	});

	void replayClosed(ReplayHandler replayHandler);
}

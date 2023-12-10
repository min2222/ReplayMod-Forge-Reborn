package com.replaymod.replay.events;

import java.io.IOException;
import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.replay.ReplayHandler;

public interface ReplayOpenedCallback {
	Event<ReplayOpenedCallback> EVENT = Event.create((listeners) -> {
		return (replayHandler) -> {
			Iterator var2 = listeners.iterator();

			while (var2.hasNext()) {
				ReplayOpenedCallback listener = (ReplayOpenedCallback) var2.next();
				listener.replayOpened(replayHandler);
			}

		};
	});

	void replayOpened(ReplayHandler replayHandler) throws IOException;
}

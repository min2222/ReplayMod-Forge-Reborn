package com.replaymod.replay.events;

import java.io.IOException;
import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.replay.ReplayHandler;

public interface ReplayClosingCallback {
	Event<ReplayClosingCallback> EVENT = Event.create((listeners) -> {
		return (replayHandler) -> {
			Iterator var2 = listeners.iterator();

			while (var2.hasNext()) {
				ReplayClosingCallback listener = (ReplayClosingCallback) var2.next();
				listener.replayClosing(replayHandler);
			}

		};
	});

	void replayClosing(ReplayHandler replayHandler) throws IOException;
}

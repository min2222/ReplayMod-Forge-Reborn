package com.replaymod.render.hooks;

import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface FogStateCallback {
	Event<FogStateCallback> EVENT = Event.create((listeners) -> {
		return (enabled) -> {
			Iterator var2 = listeners.iterator();

			while (var2.hasNext()) {
				FogStateCallback listener = (FogStateCallback) var2.next();
				listener.fogStateChanged(enabled);
			}

		};
	});

	void fogStateChanged(boolean bl);
}

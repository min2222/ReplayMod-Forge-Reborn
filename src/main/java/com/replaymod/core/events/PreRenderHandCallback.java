package com.replaymod.core.events;

import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface PreRenderHandCallback {
	Event<PreRenderHandCallback> EVENT = Event.create((listeners) -> {
		return () -> {
			Iterator var1 = listeners.iterator();

			PreRenderHandCallback listener;
			do {
				if (!var1.hasNext()) {
					return false;
				}

				listener = (PreRenderHandCallback) var1.next();
			} while (!listener.preRenderHand());

			return true;
		};
	});

	boolean preRenderHand();
}

package com.replaymod.core.events;

import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface PreRenderCallback {
	Event<PreRenderCallback> EVENT = Event.create((listeners) -> {
		return () -> {
			Iterator var1 = listeners.iterator();

			while (var1.hasNext()) {
				PreRenderCallback listener = (PreRenderCallback) var1.next();
				listener.preRender();
			}

		};
	});

	void preRender();
}

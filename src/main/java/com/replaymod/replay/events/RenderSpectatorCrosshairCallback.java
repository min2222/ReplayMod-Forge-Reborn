package com.replaymod.replay.events;

import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface RenderSpectatorCrosshairCallback {
	Event<RenderSpectatorCrosshairCallback> EVENT = Event.create((listeners) -> {
		return () -> {
			Iterator var1 = listeners.iterator();

			Boolean state;
			do {
				if (!var1.hasNext()) {
					return null;
				}

				RenderSpectatorCrosshairCallback listener = (RenderSpectatorCrosshairCallback) var1.next();
				state = listener.shouldRenderSpectatorCrosshair();
			} while (state == null);

			return state;
		};
	});

	Boolean shouldRenderSpectatorCrosshair();
}

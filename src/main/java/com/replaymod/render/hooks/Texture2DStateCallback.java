package com.replaymod.render.hooks;

import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface Texture2DStateCallback {
	Event<Texture2DStateCallback> EVENT = Event.create((listeners) -> {
		return (slot, enabled) -> {
			Iterator var3 = listeners.iterator();

			while (var3.hasNext()) {
				Texture2DStateCallback listener = (Texture2DStateCallback) var3.next();
				listener.texture2DStateChanged(slot, enabled);
			}

		};
	});

	void texture2DStateChanged(int i, boolean bl);
}

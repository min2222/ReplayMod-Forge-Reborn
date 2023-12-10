package com.replaymod.core.events;

import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface KeyEventCallback {
	Event<KeyEventCallback> EVENT = Event.create((listeners) -> {
		return (key, scanCode, action, modifiers) -> {
			Iterator var5 = listeners.iterator();

			KeyEventCallback listener;
			do {
				if (!var5.hasNext()) {
					return false;
				}

				listener = (KeyEventCallback) var5.next();
			} while (!listener.onKeyEvent(key, scanCode, action, modifiers));

			return true;
		};
	});
	int ACTION_RELEASE = 0;
	int ACTION_PRESS = 1;

	boolean onKeyEvent(int i, int j, int k, int l);
}

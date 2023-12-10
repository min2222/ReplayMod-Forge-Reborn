package com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface PreTickCallback {
	Event<PreTickCallback> EVENT = Event.create((listeners) -> () -> {
		for (PreTickCallback listener : listeners) {
			listener.preTick();
		}
	});

	void preTick();
}

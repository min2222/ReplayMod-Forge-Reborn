package com.replaymod.core.events;

import java.util.Iterator;

import com.replaymod.core.SettingsRegistry;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface SettingsChangedCallback {
	Event<SettingsChangedCallback> EVENT = Event.create((listeners) -> {
		return (registry, key) -> {
			Iterator var3 = listeners.iterator();

			while (var3.hasNext()) {
				SettingsChangedCallback listener = (SettingsChangedCallback) var3.next();
				listener.onSettingsChanged(registry, key);
			}

		};
	});

	void onSettingsChanged(SettingsRegistry settingsRegistry, SettingsRegistry.SettingKey<?> settingKey);
}

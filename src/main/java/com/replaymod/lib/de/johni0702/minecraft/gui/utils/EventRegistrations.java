package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import java.util.ArrayList;
import java.util.List;

import com.replaymod.gui.versions.forge.EventsAdapter;

import net.minecraftforge.common.MinecraftForge;

public class EventRegistrations {
	static {
		new EventsAdapter().register();
	}

	private List<EventRegistration<?>> registrations = new ArrayList<>();

	public <T> EventRegistrations on(EventRegistration<T> registration) {
		registrations.add(registration);
		return this;
	}

	public <T> EventRegistrations on(Event<T> event, T listener) {
		return on(EventRegistration.create(event, listener));
	}

	public void register() {
		MinecraftForge.EVENT_BUS.register(this);
		for (EventRegistration<?> registration : registrations) {
			registration.register();
		}
	}

	public void unregister() {
		MinecraftForge.EVENT_BUS.unregister(this);
		for (EventRegistration<?> registration : registrations) {
			registration.unregister();
		}
	}
}

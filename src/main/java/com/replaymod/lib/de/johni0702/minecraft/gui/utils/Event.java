package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Event<T> {
	private T invoker;
	private Function<List<T>, T> invokerFactory;
	private List<T> listeners = new ArrayList<T>();

	public static <T> Event<T> create(Function<List<T>, T> invokerFactory) {
		return new Event<>(invokerFactory);
	}

	private Event(Function<List<T>, T> invokerFactory) {
		this.invokerFactory = invokerFactory;
		this.update();
	}

	void register(T listener) {
		this.listeners.add(listener);
		this.update();
	}

	void unregister(T listener) {
		this.listeners.remove(listener);
		this.update();
	}

	private void update() {
		this.invoker = this.invokerFactory.apply(new ArrayList<>(this.listeners));
	}

	public T invoker() {
		return this.invoker;
	}
}

package com.replaymod.render.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class FlawlessFrames {
	private static final List<Consumer<Boolean>> CONSUMERS = new CopyOnWriteArrayList();
	private static boolean hasSodium;

	private FlawlessFrames() {
	}

	public static void registerConsumer(Function<String, Consumer<Boolean>> provider) {
		Consumer<Boolean> consumer = (Consumer) provider.apply("replaymod");
		CONSUMERS.add(consumer);
		if (provider.getClass().getName().contains(".sodium.") || consumer.getClass().getName().contains(".sodium.")) {
			hasSodium = true;
		}

	}

	public static void setEnabled(boolean enabled) {
		CONSUMERS.forEach((it) -> {
			it.accept(enabled);
		});
	}

	public static boolean hasSodium() {
		return hasSodium;
	}
}

package com.replaymod.lib.de.javagl.jgltf.model.io;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;

public final class GltfReference {
	private final String name;
	private final String uri;
	private final Consumer<ByteBuffer> target;

	public GltfReference(String name, String uri, Consumer<ByteBuffer> target) {
		this.name = (String) Objects.requireNonNull(name, "The name may not be null");
		this.uri = (String) Objects.requireNonNull(uri, "The uri may not be null");
		this.target = (Consumer) Objects.requireNonNull(target, "The target may not be null");
	}

	public String getName() {
		return this.name;
	}

	public String getUri() {
		return this.uri;
	}

	public Consumer<ByteBuffer> getTarget() {
		return this.target;
	}
}

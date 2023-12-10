package com.replaymod.lib.de.javagl.jgltf.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

class ByteBufferInputStream extends InputStream {
	private final ByteBuffer byteBuffer;

	ByteBufferInputStream(ByteBuffer byteBuffer) {
		this.byteBuffer = (ByteBuffer) Objects.requireNonNull(byteBuffer, "The byteBuffer may not be null");
	}

	public int read() throws IOException {
		return !this.byteBuffer.hasRemaining() ? -1 : this.byteBuffer.get() & 255;
	}

	public int read(byte[] bytes, int off, int len) throws IOException {
		if (!this.byteBuffer.hasRemaining()) {
			return -1;
		} else {
			int readLength = Math.min(len, this.byteBuffer.remaining());
			this.byteBuffer.get(bytes, off, readLength);
			return readLength;
		}
	}
}

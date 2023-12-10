package com.replaymod.lib.de.javagl.jgltf.model.io;

import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.Iterator;

public class Buffers {
	public static String readAsString(ByteBuffer byteBuffer) {
		if (byteBuffer == null) {
			return null;
		} else {
			byte[] array = new byte[byteBuffer.capacity()];
			byteBuffer.slice().get(array);
			return new String(array);
		}
	}

	public static ByteBuffer createSlice(ByteBuffer byteBuffer) {
		return byteBuffer == null ? null : byteBuffer.slice().order(byteBuffer.order());
	}

	public static ByteBuffer createSlice(ByteBuffer byteBuffer, int position, int length) {
		if (byteBuffer == null) {
			return null;
		} else {
			int oldPosition = byteBuffer.position();
			int oldLimit = byteBuffer.limit();

			ByteBuffer var7;
			try {
				int newLimit = position + length;
				if (newLimit > byteBuffer.capacity()) {
					throw new IllegalArgumentException(
							"The new limit is " + newLimit + ", but the capacity is " + byteBuffer.capacity());
				}

				byteBuffer.limit(newLimit);
				byteBuffer.position(position);
				ByteBuffer slice = byteBuffer.slice();
				slice.order(byteBuffer.order());
				var7 = slice;
			} finally {
				byteBuffer.limit(oldLimit);
				byteBuffer.position(oldPosition);
			}

			return var7;
		}
	}

	public static ByteBuffer create(byte[] data) {
		return create(data, 0, data.length);
	}

	public static ByteBuffer create(byte[] data, int offset, int length) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put(data, offset, length);
		byteBuffer.position(0);
		return byteBuffer;
	}

	public static ByteBuffer create(int size) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		return byteBuffer;
	}

	public static InputStream createByteBufferInputStream(ByteBuffer byteBuffer) {
		return new ByteBufferInputStream(byteBuffer);
	}

	public static ByteBuffer concat(Collection<? extends ByteBuffer> byteBuffers) {
		if (byteBuffers != null && !byteBuffers.isEmpty()) {
			int resultCapacity = byteBuffers.stream().mapToInt(Buffer::capacity).reduce(0, (a, b) -> {
				return a + b;
			});
			ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(resultCapacity).order(ByteOrder.nativeOrder());
			Iterator var3 = byteBuffers.iterator();

			while (var3.hasNext()) {
				ByteBuffer byteBuffer = (ByteBuffer) var3.next();
				newByteBuffer.put(byteBuffer.slice());
			}

			newByteBuffer.position(0);
			return newByteBuffer;
		} else {
			return ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());
		}
	}

	public static ByteBuffer createByteBufferFrom(FloatBuffer buffer) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.capacity() * 4);
		FloatBuffer floatBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		floatBuffer.put(buffer.slice());
		return byteBuffer;
	}

	public static ByteBuffer createByteBufferFrom(IntBuffer buffer) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.capacity() * 4);
		IntBuffer intBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asIntBuffer();
		intBuffer.put(buffer.slice());
		return byteBuffer;
	}

	public static ByteBuffer createByteBufferFrom(ShortBuffer buffer) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.capacity() * 2);
		ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asShortBuffer();
		shortBuffer.put(buffer.slice());
		return byteBuffer;
	}

	public static ByteBuffer castToByteBuffer(IntBuffer buffer) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.capacity()).order(ByteOrder.nativeOrder());

		for (int i = 0; i < buffer.capacity(); ++i) {
			byteBuffer.put(i, (byte) buffer.get(i));
		}

		return byteBuffer;
	}

	public static ByteBuffer castToShortByteBuffer(IntBuffer buffer) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.capacity() * 2);
		ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asShortBuffer();

		for (int i = 0; i < buffer.capacity(); ++i) {
			shortBuffer.put(i, (short) buffer.get(i));
		}

		return byteBuffer;
	}

	public static ByteBuffer copyOf(ByteBuffer buffer, int newCapacity) {
		ByteBuffer copy = ByteBuffer.allocateDirect(newCapacity);
		copy.order(buffer.order());
		if (newCapacity < buffer.capacity()) {
			copy.slice().put(createSlice(buffer, 0, newCapacity));
		} else {
			copy.slice().put(createSlice(buffer));
		}

		return copy;
	}

	public static void bufferCopy(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int length) {
		for (int i = 0; i < length; ++i) {
			byte b = src.get(srcPos + i);
			dst.put(dstPos + i, b);
		}

	}

	private Buffers() {
	}
}

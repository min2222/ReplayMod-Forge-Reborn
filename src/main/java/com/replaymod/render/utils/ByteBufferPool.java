package com.replaymod.render.utils;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;

import com.google.common.collect.Maps;

public class ByteBufferPool {
	private static Map<Integer, List<SoftReference<ByteBuffer>>> bufferPool = Maps.newHashMap();

	public static synchronized ByteBuffer allocate(int size) {
		List<SoftReference<ByteBuffer>> available = (List) bufferPool.get(size);
		if (available != null) {
			Iterator iter = available.iterator();

			try {
				while (iter.hasNext()) {
					SoftReference<ByteBuffer> reference = (SoftReference) iter.next();
					ByteBuffer buffer = (ByteBuffer) reference.get();
					iter.remove();
					if (buffer != null) {
						ByteBuffer var5 = buffer;
						return var5;
					}
				}
			} finally {
				if (!iter.hasNext()) {
					bufferPool.remove(size);
				}

			}
		}

		return BufferUtils.createByteBuffer(size);
	}

	public static synchronized void release(ByteBuffer buffer) {
		buffer.clear();
		int size = buffer.capacity();
		List<SoftReference<ByteBuffer>> available = (List) bufferPool.get(size);
		if (available == null) {
			available = new LinkedList();
			bufferPool.put(size, available);
		}

		((List) available).add(new SoftReference(buffer));
	}
}

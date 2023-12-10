package com.replaymod.render.utils;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL15;

import com.google.common.base.Objects;
import com.replaymod.core.ReplayMod;

public class PixelBufferObject {
	public static final boolean SUPPORTED;
	private static final boolean arb;
	private static ThreadLocal<Integer> bound;
	private static ThreadLocal<Integer> mapped;
	private final long size;
	private long handle;

	public PixelBufferObject(long size, PixelBufferObject.Usage usage) {
		if (!SUPPORTED) {
			throw new UnsupportedOperationException("PBOs not supported.");
		} else {
			this.size = size;
			this.handle = arb ? (long) ARBVertexBufferObject.glGenBuffersARB() : (long) GL15.glGenBuffers();
			this.bind();
			if (arb) {
				ARBVertexBufferObject.glBufferDataARB(35051, size, usage.arb);
			} else {
				GL15.glBufferData(35051, size, usage.gl15);
			}

			this.unbind();
		}
	}

	private int getHandle() {
		if (this.handle == -1L) {
			throw new IllegalStateException("PBO not allocated.");
		} else {
			return (int) this.handle;
		}
	}

	public void bind() {
		if (arb) {
			ARBVertexBufferObject.glBindBufferARB(35051, this.getHandle());
		} else {
			GL15.glBindBuffer(35051, this.getHandle());
		}

		bound.set(this.getHandle());
	}

	public void unbind() {
		this.checkBound();
		if (arb) {
			ARBVertexBufferObject.glBindBufferARB(35051, 0);
		} else {
			GL15.glBindBuffer(35051, 0);
		}

		bound.set(0);
	}

	private void checkBound() {
		if (!Objects.equal(this.getHandle(), bound.get())) {
			throw new IllegalStateException("Buffer not bound.");
		}
	}

	private void checkNotMapped() {
		if (Objects.equal(this.getHandle(), mapped.get())) {
			throw new IllegalStateException("Buffer already mapped.");
		}
	}

	public ByteBuffer mapReadOnly() {
		this.checkBound();
		this.checkNotMapped();
		ByteBuffer buffer;
		if (arb) {
			buffer = ARBVertexBufferObject.glMapBufferARB(35051, 35000, this.size, (ByteBuffer) null);
		} else {
			buffer = GL15.glMapBuffer(35051, 35000, this.size, (ByteBuffer) null);
		}

		mapped.set(this.getHandle());
		return buffer;
	}

	public ByteBuffer mapWriteOnly() {
		this.checkBound();
		this.checkNotMapped();
		ByteBuffer buffer;
		if (arb) {
			buffer = ARBVertexBufferObject.glMapBufferARB(35051, 35001, this.size, (ByteBuffer) null);
		} else {
			buffer = GL15.glMapBuffer(35051, 35001, this.size, (ByteBuffer) null);
		}

		mapped.set(this.getHandle());
		return buffer;
	}

	public ByteBuffer mapReadWrite() {
		this.checkBound();
		this.checkNotMapped();
		ByteBuffer buffer;
		if (arb) {
			buffer = ARBVertexBufferObject.glMapBufferARB(35051, 35002, this.size, (ByteBuffer) null);
		} else {
			buffer = GL15.glMapBuffer(35051, 35002, this.size, (ByteBuffer) null);
		}

		mapped.set(this.getHandle());
		return buffer;
	}

	public void unmap() {
		this.checkBound();
		if (!Objects.equal(mapped.get(), this.getHandle())) {
			throw new IllegalStateException("Buffer not mapped.");
		} else {
			if (arb) {
				ARBVertexBufferObject.glUnmapBufferARB(35051);
			} else {
				GL15.glUnmapBuffer(35051);
			}

			mapped.set(0);
		}
	}

	public void delete() {
		if (this.handle != -1L) {
			if (arb) {
				ARBVertexBufferObject.glDeleteBuffersARB(this.getHandle());
			} else {
				GL15.glDeleteBuffers(this.getHandle());
			}

			this.handle = -1L;
		}

	}

	protected void finalize() throws Throwable {
		super.finalize();
		if (this.handle != -1L) {
			LogManager.getLogger().warn("PBO garbage collected before deleted!");
			ReplayMod.instance.runLater(this::delete);
		}

	}

	static {
		SUPPORTED = GL.getCapabilities().GL_ARB_pixel_buffer_object || GL.getCapabilities().OpenGL15;
		arb = !GL.getCapabilities().OpenGL15;
		bound = new ThreadLocal();
		mapped = new ThreadLocal();
	}

	public static enum Usage {
		COPY(35042, 35042), DRAW(35040, 35040), READ(35041, 35041);

		private final int arb;
		private final int gl15;

		private Usage(int arb, int gl15) {
			this.arb = arb;
			this.gl15 = gl15;
		}

		// $FF: synthetic method
		private static PixelBufferObject.Usage[] $values() {
			return new PixelBufferObject.Usage[] { COPY, DRAW, READ };
		}
	}
}

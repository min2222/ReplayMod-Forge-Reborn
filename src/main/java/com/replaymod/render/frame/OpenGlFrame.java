package com.replaymod.render.frame;

import java.nio.ByteBuffer;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.rendering.Frame;

public class OpenGlFrame implements Frame {
	private final int frameId;
	private final ReadableDimension size;
	private final int bytesPerPixel;
	private final ByteBuffer byteBuffer;

	public OpenGlFrame(int frameId, ReadableDimension size, int bytesPerPixel, ByteBuffer byteBuffer) {
		this.frameId = frameId;
		this.size = size;
		this.bytesPerPixel = bytesPerPixel;
		this.byteBuffer = byteBuffer;
	}

	public int getFrameId() {
		return this.frameId;
	}

	public ReadableDimension getSize() {
		return this.size;
	}

	public int getBytesPerPixel() {
		return this.bytesPerPixel;
	}

	public ByteBuffer getByteBuffer() {
		return this.byteBuffer;
	}
}

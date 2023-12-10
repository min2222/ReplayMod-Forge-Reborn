package com.replaymod.render.frame;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.Validate;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.rendering.Frame;

public class BitmapFrame implements Frame {
	private final int frameId;
	private final ReadableDimension size;
	private final int bytesPerPixel;
	private final ByteBuffer byteBuffer;

	public BitmapFrame(int frameId, ReadableDimension size, int bytesPerPixel, ByteBuffer byteBuffer) {
		Validate.isTrue(size.getWidth() * size.getHeight() * bytesPerPixel == byteBuffer.remaining(),
				"Buffer size is %d (cap: %d) but should be %d", byteBuffer.remaining(), byteBuffer.capacity(),
				size.getWidth() * size.getHeight() * bytesPerPixel);
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

package com.replaymod.render.processor;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.frame.ODSOpenGlFrame;
import com.replaymod.render.utils.ByteBufferPool;

public class ODSToBitmapProcessor extends AbstractFrameProcessor<ODSOpenGlFrame, BitmapFrame> {
	private final EquirectangularToBitmapProcessor processor;

	public ODSToBitmapProcessor(int outputWidth, int outputHeight, int sphericalFovX) {
		this.processor = new EquirectangularToBitmapProcessor(outputWidth, outputHeight / 2, sphericalFovX);
	}

	public BitmapFrame process(ODSOpenGlFrame rawFrame) {
		BitmapFrame leftFrame = this.processor.process(rawFrame.getLeft());
		BitmapFrame rightFrame = this.processor.process(rawFrame.getRight());
		ReadableDimension size = new Dimension(leftFrame.getSize().getWidth(), leftFrame.getSize().getHeight() * 2);
		int bpp = rawFrame.getLeft().getLeft().getBytesPerPixel();
		ByteBuffer result = ByteBufferPool.allocate(size.getWidth() * size.getHeight() * bpp);
		result.put(leftFrame.getByteBuffer());
		result.put(rightFrame.getByteBuffer());
		result.rewind();
		ByteBufferPool.release(leftFrame.getByteBuffer());
		ByteBufferPool.release(rightFrame.getByteBuffer());
		return new BitmapFrame(rawFrame.getFrameId(), size, bpp, result);
	}

	public void close() throws IOException {
		this.processor.close();
	}

	public int getFrameSize() {
		return this.processor.getFrameSize();
	}
}

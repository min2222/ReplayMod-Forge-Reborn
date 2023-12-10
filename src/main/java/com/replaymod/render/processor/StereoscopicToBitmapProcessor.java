package com.replaymod.render.processor;

import java.nio.ByteBuffer;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.frame.StereoscopicOpenGlFrame;
import com.replaymod.render.utils.ByteBufferPool;
import com.replaymod.render.utils.Utils;

public class StereoscopicToBitmapProcessor extends AbstractFrameProcessor<StereoscopicOpenGlFrame, BitmapFrame> {
	public BitmapFrame process(StereoscopicOpenGlFrame rawFrame) {
		ReadableDimension size = rawFrame.getLeft().getSize();
		int width = size.getWidth();
		int bpp = rawFrame.getLeft().getBytesPerPixel();
		ByteBuffer result = ByteBufferPool.allocate(width * 2 * size.getHeight() * bpp);
		Utils.openGlBytesToBitmap(rawFrame.getLeft(), 0, 0, result, width * 2);
		Utils.openGlBytesToBitmap(rawFrame.getRight(), size.getWidth(), 0, result, width * 2);
		ByteBufferPool.release(rawFrame.getLeft().getByteBuffer());
		ByteBufferPool.release(rawFrame.getRight().getByteBuffer());
		return new BitmapFrame(rawFrame.getFrameId(), new Dimension(width * 2, size.getHeight()), bpp, result);
	}
}

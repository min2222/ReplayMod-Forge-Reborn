package com.replaymod.render.processor;

import java.nio.ByteBuffer;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.utils.ByteBufferPool;
import com.replaymod.render.utils.Utils;

public class OpenGlToBitmapProcessor extends AbstractFrameProcessor<OpenGlFrame, BitmapFrame> {
	public BitmapFrame process(OpenGlFrame rawFrame) {
		ReadableDimension size = rawFrame.getSize();
		int width = size.getWidth();
		int height = size.getHeight();
		int bpp = rawFrame.getBytesPerPixel();
		ByteBuffer result = ByteBufferPool.allocate(width * height * bpp);
		Utils.openGlBytesToBitmap(rawFrame, 0, 0, result, width);
		ByteBufferPool.release(rawFrame.getByteBuffer());
		return new BitmapFrame(rawFrame.getFrameId(), new Dimension(width, height), bpp, result);
	}
}

package com.replaymod.render.utils;

import java.nio.ByteBuffer;

import com.replaymod.render.frame.OpenGlFrame;

public class Utils {
	public static void openGlBytesToBitmap(OpenGlFrame source, int xOffset, int yOffset, ByteBuffer to, int width) {
		openGlBytesToBitmap(source.getByteBuffer(), source.getSize().getWidth(), source.getBytesPerPixel(), xOffset,
				yOffset, to, width);
	}

	public static void openGlBytesToBitmap(ByteBuffer buffer, int bufferWidth, int bbp, int xOffset, int yOffset,
			ByteBuffer to, int width) {
		byte[] rowBuf = new byte[bufferWidth * bbp];
		int rows = buffer.remaining() / bbp / bufferWidth;

		for (int i = 0; i < rows; ++i) {
			buffer.get(rowBuf);
			to.position(((yOffset + rows - i - 1) * width + xOffset) * bbp);
			to.put(rowBuf);
		}

		to.rewind();
	}
}

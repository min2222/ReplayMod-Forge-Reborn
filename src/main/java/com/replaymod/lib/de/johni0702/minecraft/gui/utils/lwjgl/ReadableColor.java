package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl;

import java.nio.ByteBuffer;

public interface ReadableColor {
	ReadableColor RED = new Color(255, 0, 0);
	ReadableColor ORANGE = new Color(255, 128, 0);
	ReadableColor YELLOW = new Color(255, 255, 0);
	ReadableColor GREEN = new Color(0, 255, 0);
	ReadableColor CYAN = new Color(0, 255, 255);
	ReadableColor BLUE = new Color(0, 0, 255);
	ReadableColor PURPLE = new Color(255, 0, 255);
	ReadableColor WHITE = new Color(255, 255, 255);
	ReadableColor BLACK = new Color(0, 0, 0);
	ReadableColor LTGREY = new Color(192, 192, 192);
	ReadableColor DKGREY = new Color(64, 64, 64);
	ReadableColor GREY = new Color(128, 128, 128);

	int getRed();

	int getGreen();

	int getBlue();

	int getAlpha();

	byte getRedByte();

	byte getGreenByte();

	byte getBlueByte();

	byte getAlphaByte();

	void writeRGBA(ByteBuffer byteBuffer);

	void writeRGB(ByteBuffer byteBuffer);

	void writeABGR(ByteBuffer byteBuffer);

	void writeBGR(ByteBuffer byteBuffer);

	void writeBGRA(ByteBuffer byteBuffer);

	void writeARGB(ByteBuffer byteBuffer);
}

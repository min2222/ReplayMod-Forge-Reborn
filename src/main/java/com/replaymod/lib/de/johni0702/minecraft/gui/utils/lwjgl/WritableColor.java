package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl;

import java.nio.ByteBuffer;

public interface WritableColor {
	void set(int i, int j, int k, int l);

	void set(byte b, byte c, byte d, byte e);

	void set(int i, int j, int k);

	void set(byte b, byte c, byte d);

	void setRed(int i);

	void setGreen(int i);

	void setBlue(int i);

	void setAlpha(int i);

	void setRed(byte b);

	void setGreen(byte b);

	void setBlue(byte b);

	void setAlpha(byte b);

	void readRGBA(ByteBuffer byteBuffer);

	void readRGB(ByteBuffer byteBuffer);

	void readARGB(ByteBuffer byteBuffer);

	void readBGRA(ByteBuffer byteBuffer);

	void readBGR(ByteBuffer byteBuffer);

	void readABGR(ByteBuffer byteBuffer);

	void setColor(ReadableColor readableColor);
}

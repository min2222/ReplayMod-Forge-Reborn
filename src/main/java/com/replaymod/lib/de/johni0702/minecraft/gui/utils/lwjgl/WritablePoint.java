package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl;

public interface WritablePoint {
	void setLocation(int i, int j);

	void setLocation(ReadablePoint readablePoint);

	void setX(int i);

	void setY(int i);
}

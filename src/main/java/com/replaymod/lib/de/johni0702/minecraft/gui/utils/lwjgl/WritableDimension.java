package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl;

public interface WritableDimension {
	void setSize(int i, int j);

	void setSize(ReadableDimension readableDimension);

	void setHeight(int i);

	void setWidth(int i);
}

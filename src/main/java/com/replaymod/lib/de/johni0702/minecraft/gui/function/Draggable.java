package com.replaymod.lib.de.johni0702.minecraft.gui.function;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public interface Draggable extends Clickable {
	boolean mouseDrag(ReadablePoint readablePoint, int i, @Deprecated long l);

	boolean mouseRelease(ReadablePoint readablePoint, int i);
}

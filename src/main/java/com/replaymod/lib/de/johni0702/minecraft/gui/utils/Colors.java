package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface Colors extends ReadableColor {
	ReadableColor TRANSPARENT = new Color(0, 0, 0, 0);
	ReadableColor LIGHT_TRANSPARENT = new Color(0, 0, 0, 64);
	ReadableColor HALF_TRANSPARENT = new Color(0, 0, 0, 128);
	ReadableColor DARK_TRANSPARENT = new Color(0, 0, 0, 192);
	ReadableColor LIGHT_GRAY = new Color(192, 192, 192);
	ReadableColor DARK_RED = new Color(170, 0, 0);
}

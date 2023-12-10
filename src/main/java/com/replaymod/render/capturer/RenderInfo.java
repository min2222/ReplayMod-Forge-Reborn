package com.replaymod.render.capturer;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.RenderSettings;

public interface RenderInfo {
	ReadableDimension getFrameSize();

	int getFramesDone();

	int getTotalFrames();

	float updateForNextFrame();

	RenderSettings getRenderSettings();
}

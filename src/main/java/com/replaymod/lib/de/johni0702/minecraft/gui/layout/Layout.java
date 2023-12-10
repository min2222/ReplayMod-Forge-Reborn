package com.replaymod.lib.de.johni0702.minecraft.gui.layout;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public interface Layout {
	Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> guiContainer,
			ReadableDimension readableDimension);

	ReadableDimension calcMinSize(GuiContainer<?> guiContainer);
}

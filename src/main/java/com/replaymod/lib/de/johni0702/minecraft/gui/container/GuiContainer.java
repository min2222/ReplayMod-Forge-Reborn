package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import java.util.Comparator;
import java.util.Map;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.ComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface GuiContainer<T extends GuiContainer<T>> extends ComposedGuiElement<T> {
	T setLayout(Layout layout);

	Layout getLayout();

	void convertFor(GuiElement guiElement, Point point);

	void convertFor(GuiElement guiElement, Point point, int i);

	Map<GuiElement, LayoutData> getElements();

	T addElements(LayoutData layoutData, GuiElement... guiElements);

	T removeElement(GuiElement guiElement);

	T sortElements();

	T sortElements(Comparator<GuiElement> comparator);

	ReadableColor getBackgroundColor();

	T setBackgroundColor(ReadableColor readableColor);
}

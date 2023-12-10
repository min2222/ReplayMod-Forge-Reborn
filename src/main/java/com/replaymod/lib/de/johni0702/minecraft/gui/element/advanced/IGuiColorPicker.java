package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiColorPicker<T extends IGuiColorPicker<T>> extends GuiElement<T> {
	T setColor(ReadableColor readableColor);

	ReadableColor getColor();

	T setOpened(boolean bl);

	boolean isOpened();

	T onSelection(Consumer<ReadableColor> consumer);
}

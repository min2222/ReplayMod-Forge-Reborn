package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiLabel<T extends IGuiLabel<T>> extends GuiElement<T> {
	T setText(String string);

	T setI18nText(String string, Object... objects);

	T setColor(ReadableColor readableColor);

	T setDisabledColor(ReadableColor readableColor);

	String getText();

	ReadableColor getColor();

	ReadableColor getDisabledColor();
}

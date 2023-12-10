package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiTextArea<T extends IGuiTextArea<T>> extends GuiElement<T>, Focusable<T> {
	T setText(String[] strings);

	String[] getText();

	String getText(int i, int j, int k, int l);

	int getSelectionFromX();

	int getSelectionToX();

	int getSelectionFromY();

	int getSelectionToY();

	String getSelectedText();

	void deleteSelectedText();

	String cutSelectedText();

	void writeText(String string);

	void writeChar(char c);

	T setCursorPosition(int i, int j);

	T setMaxTextWidth(int i);

	T setMaxTextHeight(int i);

	T setMaxCharCount(int i);

	T setTextColor(ReadableColor readableColor);

	T setTextColorDisabled(ReadableColor readableColor);

	int getMaxTextWidth();

	int getMaxTextHeight();

	int getMaxCharCount();

	String[] getHint();

	T setHint(String... strings);

	T setI18nHint(String string, Object... objects);
}

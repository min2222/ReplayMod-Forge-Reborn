package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.NonNull;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiTextField<T extends IGuiTextField<T>> extends GuiElement<T>, Focusable<T> {
	@NonNull
	T setText(String string);

	@NonNull
	T setI18nText(String string, Object... objects);

	@NonNull
	String getText();

	int getMaxLength();

	T setMaxLength(int i);

	@NonNull
	String deleteText(int i, int j);

	int getSelectionFrom();

	int getSelectionTo();

	@NonNull
	String getSelectedText();

	@NonNull
	String deleteSelectedText();

	@NonNull
	T writeText(String string);

	@NonNull
	T writeChar(char c);

	T deleteNextChar();

	String deleteNextWord();

	@NonNull
	T deletePreviousChar();

	@NonNull
	String deletePreviousWord();

	@NonNull
	T setCursorPosition(int i);

	T onEnter(Runnable runnable);

	T onTextChanged(Consumer<String> consumer);

	String getHint();

	T setHint(String string);

	T setI18nHint(String string, Object... objects);

	ReadableColor getTextColor();

	T setTextColor(ReadableColor readableColor);

	ReadableColor getTextColorDisabled();

	T setTextColorDisabled(ReadableColor readableColor);
}

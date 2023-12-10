package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;

public interface IGuiProgressBar<T extends IGuiProgressBar<T>> extends GuiElement<T> {
	T setProgress(float f);

	T setLabel(String string);

	T setI18nLabel(String string, Object... objects);

	float getProgress();

	String getLabel();
}

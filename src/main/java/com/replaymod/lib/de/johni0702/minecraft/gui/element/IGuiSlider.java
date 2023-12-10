package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiSlider<T extends IGuiSlider<T>> extends GuiElement<T> {
	T setText(String string);

	T setI18nText(String string, Object... objects);

	T setValue(int i);

	int getValue();

	int getSteps();

	T setSteps(int i);

	T onValueChanged(Runnable runnable);
}

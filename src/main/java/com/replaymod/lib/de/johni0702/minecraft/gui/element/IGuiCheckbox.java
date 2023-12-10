package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiCheckbox<T extends IGuiCheckbox<T>> extends IGuiClickable<T> {
	T setLabel(String string);

	T setI18nLabel(String string, Object... objects);

	T setChecked(boolean bl);

	String getLabel();

	boolean isChecked();
}

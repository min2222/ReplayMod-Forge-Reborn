package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiNumberField<T extends IGuiNumberField<T>> extends IGuiTextField<T> {
	byte getByte();

	short getShort();

	int getInteger();

	long getLong();

	float getFloat();

	double getDouble();

	T setValue(int i);

	T setValue(double d);

	T setMinValue(Double double_);

	T setMaxValue(Double double_);

	T setMinValue(int i);

	T setMaxValue(int i);

	T setValidateOnFocusChange(boolean bl);

	T setPrecision(int i);
}

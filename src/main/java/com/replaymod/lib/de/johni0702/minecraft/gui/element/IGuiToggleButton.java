package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiToggleButton<V, T extends IGuiToggleButton<V, T>> extends IGuiButton<T> {
	T setValues(V[] objects);

	T setSelected(int i);

	V getSelectedValue();

	int getSelected();

	V[] getValues();
}

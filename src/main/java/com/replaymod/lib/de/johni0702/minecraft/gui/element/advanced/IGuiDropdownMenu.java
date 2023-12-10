package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import java.util.Map;
import java.util.function.Function;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;

public interface IGuiDropdownMenu<V, T extends IGuiDropdownMenu<V, T>> extends GuiElement<T> {
	T setValues(V... objects);

	T setSelected(int i);

	T setSelected(V object);

	V getSelectedValue();

	T setOpened(boolean bl);

	int getSelected();

	V[] getValues();

	boolean isOpened();

	T onSelection(Consumer<Integer> consumer);

	Map<V, IGuiClickable> getDropdownEntries();

	T setToString(Function<V, String> function);
}

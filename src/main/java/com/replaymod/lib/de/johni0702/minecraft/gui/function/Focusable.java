package com.replaymod.lib.de.johni0702.minecraft.gui.function;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;

public interface Focusable<T extends Focusable<T>> {
	boolean isFocused();

	T setFocused(boolean bl);

	T onFocusChange(Consumer<Boolean> consumer);

	Focusable getNext();

	T setNext(Focusable focusable);

	Focusable getPrevious();

	T setPrevious(Focusable focusable);
}

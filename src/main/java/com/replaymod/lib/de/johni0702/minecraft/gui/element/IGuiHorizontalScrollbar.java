package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiHorizontalScrollbar<T extends IGuiHorizontalScrollbar<T>> extends GuiElement<T> {
	T setPosition(double d);

	double getPosition();

	T setZoom(double d);

	double getZoom();

	T onValueChanged(Runnable runnable);
}

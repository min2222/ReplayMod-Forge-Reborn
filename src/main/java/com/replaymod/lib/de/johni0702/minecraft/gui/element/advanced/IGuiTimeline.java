package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;

public interface IGuiTimeline<T extends IGuiTimeline<T>> extends GuiElement<T> {
	T setLength(int i);

	int getLength();

	T setCursorPosition(int i);

	int getCursorPosition();

	T setZoom(double d);

	double getZoom();

	T setOffset(int i);

	int getOffset();

	T setMarkers();

	T setMarkers(boolean bl);

	boolean getMarkers();

	int getMarkerInterval();

	T setCursor(boolean bl);

	boolean getCursor();

	T onClick(IGuiTimeline.OnClick onClick);

	public interface OnClick {
		void run(int i);
	}
}

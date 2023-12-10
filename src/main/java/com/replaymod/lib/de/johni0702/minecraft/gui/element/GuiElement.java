package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

import net.minecraft.client.Minecraft;

public interface GuiElement<T extends GuiElement<T>> {
	Minecraft getMinecraft();

	GuiContainer getContainer();

	T setContainer(GuiContainer guiContainer);

	void layout(ReadableDimension readableDimension, RenderInfo renderInfo);

	void draw(GuiRenderer guiRenderer, ReadableDimension readableDimension, RenderInfo renderInfo);

	ReadableDimension getMinSize();

	ReadableDimension getMaxSize();

	T setMaxSize(ReadableDimension readableDimension);

	boolean isEnabled();

	T setEnabled(boolean bl);

	T setEnabled();

	T setDisabled();

	GuiElement getTooltip(RenderInfo renderInfo);

	T setTooltip(GuiElement guiElement);

	int getLayer();
}

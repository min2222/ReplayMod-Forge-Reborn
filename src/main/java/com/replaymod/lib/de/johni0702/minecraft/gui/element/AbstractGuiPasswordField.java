package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import org.apache.commons.lang3.StringUtils;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

public abstract class AbstractGuiPasswordField<T extends AbstractGuiPasswordField<T>> extends AbstractGuiTextField<T> {
	public AbstractGuiPasswordField() {
	}

	public AbstractGuiPasswordField(GuiContainer container) {
		super(container);
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		String text = this.getText();
		this.setText(StringUtils.repeat('*', text.length()));
		super.draw(renderer, size, renderInfo);
		this.setText(text);
	}
}

package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import java.util.Objects;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;

public abstract class AbstractGuiProgressBar<T extends AbstractGuiProgressBar<T>> extends AbstractGuiElement<T>
		implements IGuiProgressBar<T> {
	private static final int BORDER = 2;
	private float progress;
	private String label = "%d%%";

	public AbstractGuiProgressBar() {
	}

	public AbstractGuiProgressBar(GuiContainer container) {
		super(container);
	}

	public T setProgress(float progress) {
		this.progress = progress;
		return this.getThis();
	}

	public T setLabel(String label) {
		this.label = label;
		return this.getThis();
	}

	public T setI18nLabel(String label, Object... args) {
		return this.setLabel(I18n.get(label, args));
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		Font fontRenderer = MCVer.getFontRenderer();
		int width = size.getWidth();
		int height = size.getHeight();
		int barTotalWidth = width - 4;
		int barDoneWidth = (int) ((float) barTotalWidth * this.progress);
		renderer.drawRect(0, 0, width, height, ReadableColor.BLACK);
		renderer.drawRect(2, 2, barTotalWidth, height - 4, ReadableColor.WHITE);
		renderer.drawRect(2, 2, barDoneWidth, height - 4, ReadableColor.GREY);
		String text = String.format(this.label, (int) (this.progress * 100.0F));
		int var10001 = width / 2;
		int var10002 = size.getHeight() / 2;
		Objects.requireNonNull(fontRenderer);
		renderer.drawCenteredString(var10001, var10002 - 9 / 2, ReadableColor.BLACK, text);
	}

	public ReadableDimension calcMinSize() {
		return new Dimension(0, 0);
	}

	public float getProgress() {
		return this.progress;
	}

	public String getLabel() {
		return this.label;
	}
}

package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import java.util.Objects;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.StringUtils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;

public abstract class AbstractGuiTooltip<T extends AbstractGuiTooltip<T>> extends AbstractGuiElement<T> {
	private static final int LINE_SPACING = 3;
	private static final ReadableColor BACKGROUND_COLOR = new Color(16, 0, 16, 240);
	private static final ReadableColor BORDER_LIGHT = new Color(80, 0, 255, 80);
	private static final ReadableColor BORDER_DARK = new Color(40, 0, 127, 80);
	private String[] text = new String[0];
	private ReadableColor color;

	public AbstractGuiTooltip() {
		this.color = ReadableColor.WHITE;
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);

		int width = size.getWidth();
		int height = size.getHeight();

		// Draw background
		renderer.drawRect(1, 0, width - 2, height, BACKGROUND_COLOR); // Top to bottom
		renderer.drawRect(0, 1, 1, height - 2, BACKGROUND_COLOR); // Left pixel row
		renderer.drawRect(width - 1, 1, 1, height - 2, BACKGROUND_COLOR); // Right pixel row

		// Draw the border, it gets darker from top to bottom
		renderer.drawRect(1, 1, width - 2, 1, BORDER_LIGHT); // Top border
		renderer.drawRect(1, height - 2, width - 2, 1, BORDER_DARK); // Bottom border
		renderer.drawRect(1, 2, 1, height - 4, BORDER_LIGHT, BORDER_LIGHT, BORDER_DARK, BORDER_DARK); // Left border
		renderer.drawRect(width - 2, 2, 1, height - 4, BORDER_LIGHT, BORDER_LIGHT, BORDER_DARK, BORDER_DARK); // Right
																												// border

		Font fontRenderer = MCVer.getFontRenderer();
		int y = LINE_SPACING + 1;
		for (String line : text) {
			renderer.drawString(LINE_SPACING + 1, y, color, line, true);
			y += fontRenderer.lineHeight + LINE_SPACING;
		}
	}

	public ReadableDimension calcMinSize() {
		Font fontRenderer = MCVer.getFontRenderer();
		int var10001 = this.text.length;
		Objects.requireNonNull(fontRenderer);
		int height = 4 + var10001 * (9 + 3);
		int width = 0;
		String[] var4 = this.text;
		int var5 = var4.length;

		for (int var6 = 0; var6 < var5; ++var6) {
			String line = var4[var6];
			int w = fontRenderer.width(line);
			if (w > width) {
				width = w;
			}
		}

		width += 8;
		return new Dimension(width, height);
	}

	public ReadableDimension getMaxSize() {
		return this.getMinSize();
	}

	public T setText(String[] text) {
		this.text = text;
		return this.getThis();
	}

	public T setText(String text) {
		return this.setText(StringUtils.splitStringInMultipleRows(text, 250));
	}

	public T setI18nText(String text, Object... args) {
		return this.setText(I18n.get(text, args));
	}

	public T setColor(ReadableColor color) {
		this.color = color;
		return this.getThis();
	}

	public String[] getText() {
		return this.text;
	}

	public ReadableColor getColor() {
		return this.color;
	}
}

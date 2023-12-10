package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;

public abstract class AbstractGuiLabel<T extends AbstractGuiLabel<T>> extends AbstractGuiElement<T>
		implements IGuiLabel<T> {
	private String text = "";
	private ReadableColor color;
	private ReadableColor disabledColor;

	public AbstractGuiLabel() {
		this.color = ReadableColor.WHITE;
		this.disabledColor = ReadableColor.GREY;
	}

	public AbstractGuiLabel(GuiContainer container) {
		super(container);
		this.color = ReadableColor.WHITE;
		this.disabledColor = ReadableColor.GREY;
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		Font fontRenderer = MCVer.getFontRenderer();
		List<String> lines = fontRenderer.getSplitter()
				.splitLines(MCVer.literalText(this.text), size.getWidth(), Style.EMPTY).stream().map((it) -> {
					return it.visit(Optional::of);
				}).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		int y = 0;

		for (Iterator var7 = lines.iterator(); var7.hasNext(); y += 9) {
			String line = (String) var7.next();
			renderer.drawString(0, y, this.isEnabled() ? this.color : this.disabledColor, line);
			Objects.requireNonNull(fontRenderer);
		}

	}

	public ReadableDimension calcMinSize() {
		Font fontRenderer = MCVer.getFontRenderer();
		int var10002 = fontRenderer.width(this.text);
		Objects.requireNonNull(fontRenderer);
		return new Dimension(var10002, 9);
	}

	public ReadableDimension getMaxSize() {
		return this.getMinSize();
	}

	public T setText(String text) {
		this.text = text;
		return this.getThis();
	}

	public T setI18nText(String text, Object... args) {
		return this.setText(I18n.get(text, args));
	}

	public T setColor(ReadableColor color) {
		this.color = color;
		return this.getThis();
	}

	public T setDisabledColor(ReadableColor disabledColor) {
		this.disabledColor = disabledColor;
		return this.getThis();
	}

	public String getText() {
		return this.text;
	}

	public ReadableColor getColor() {
		return this.color;
	}

	public ReadableColor getDisabledColor() {
		return this.disabledColor;
	}
}

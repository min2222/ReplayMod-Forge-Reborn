package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import java.util.Objects;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGuiCheckbox<T extends AbstractGuiCheckbox<T>> extends AbstractGuiClickable<T>
		implements IGuiCheckbox<T> {
	protected static final ResourceLocation BUTTON_SOUND = new ResourceLocation("gui.button.press");
	protected static final ReadableColor BOX_BACKGROUND_COLOR = new Color(46, 46, 46);
	private String label;
	private boolean checked;

	public AbstractGuiCheckbox() {
	}

	public AbstractGuiCheckbox(GuiContainer container) {
		super(container);
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		int color = 14737632;
		if (!this.isEnabled()) {
			color = 10526880;
		}

		int boxSize = size.getHeight();
		renderer.drawRect(0, 0, boxSize, boxSize, ReadableColor.BLACK);
		renderer.drawRect(1, 1, boxSize - 2, boxSize - 2, BOX_BACKGROUND_COLOR);
		if (this.isChecked()) {
			renderer.drawCenteredString(boxSize / 2 + 1, 1, color, "x", true);
		}

		renderer.drawString(boxSize + 2, 2, color, this.label);
	}

	public ReadableDimension calcMinSize() {
		Font fontRenderer = MCVer.getFontRenderer();
		Objects.requireNonNull(fontRenderer);
		int height = 9 + 2;
		int width = height + 2 + fontRenderer.width(this.label);
		return new Dimension(width, height);
	}

	public ReadableDimension getMaxSize() {
		return this.getMinSize();
	}

	public void onClick() {
		AbstractGuiButton.playClickSound(this.getMinecraft());
		this.setChecked(!this.isChecked());
		super.onClick();
	}

	public T setLabel(String label) {
		this.label = label;
		return this.getThis();
	}

	public T setI18nLabel(String label, Object... args) {
		return this.setLabel(I18n.get(label, args));
	}

	public T setChecked(boolean checked) {
		this.checked = checked;
		return this.getThis();
	}

	public String getLabel() {
		return this.label;
	}

	public boolean isChecked() {
		return this.checked;
	}
}

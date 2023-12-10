package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import java.util.Collection;
import java.util.Collections;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public abstract class AbstractGuiColorPicker<T extends AbstractGuiColorPicker<T>> extends AbstractComposedGuiElement<T>
		implements IGuiColorPicker<T>, Clickable {
	protected static final int PICKER_SIZE = 100;
	private static final ReadableColor OUTLINE_COLOR = new Color(255, 255, 255);
	private Color color = new Color();
	private boolean opened;
	private Consumer<ReadableColor> onSelection;
	private AbstractGuiColorPicker<T>.GuiPicker picker = new AbstractGuiColorPicker.GuiPicker();

	public AbstractGuiColorPicker() {
	}

	public AbstractGuiColorPicker(GuiContainer container) {
		super(container);
	}

	public int getMaxLayer() {
		return this.opened ? 1 : 0;
	}

	protected ReadableDimension calcMinSize() {
		return new Dimension(3, 3);
	}

	public void layout(ReadableDimension size, RenderInfo renderInfo) {
		super.layout(size, renderInfo);
		if (size != null) {
			if (renderInfo.layer == 1) {
				ReadableDimension offsetSize = new Dimension(100, 100);
				this.picker.layout(offsetSize, renderInfo);
			}

		}
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		if (renderInfo.layer == 0) {
			int width = size.getWidth();
			int height = size.getHeight();
			renderer.drawRect(0, 0, width, height, OUTLINE_COLOR);
			renderer.drawRect(1, 1, width - 2, height - 2, this.color);
		} else if (renderInfo.layer == 1) {
			ReadablePoint offsetPoint = new Point(0, size.getHeight());
			ReadableDimension offsetSize = new Dimension(100, 100);
			OffsetGuiRenderer offsetRenderer = new OffsetGuiRenderer(renderer, offsetPoint, offsetSize);
			offsetRenderer.startUsing();

			try {
				this.picker.draw(offsetRenderer, offsetSize, renderInfo);
			} finally {
				offsetRenderer.stopUsing();
			}
		}

	}

	protected void getColorAtPosition(int x, int y, Color color) {
		if (x >= 0 && y >= 0 && x < 100 && y < 100) {
			if (x < 5) {
				int intensity = 255 - y * 255 / 100;
				color.set(intensity, intensity, intensity);
			} else {
				float hue = ((float) x - 5.0F) / 95.0F;
				float saturation = Math.min((float) y / 50.0F, 1.0F);
				float brightness = Math.min(2.0F - (float) y / 50.0F, 1.0F);
				color.fromHSB(hue, saturation, brightness);
			}

		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	public T setColor(ReadableColor color) {
		this.color.setColor(color);
		return this.getThis();
	}

	public T setOpened(boolean opened) {
		this.opened = opened;
		return this.getThis();
	}

	public Collection<GuiElement> getChildren() {
		return (Collection) (this.opened ? Collections.singleton(this.picker) : Collections.emptyList());
	}

	public T onSelection(Consumer<ReadableColor> consumer) {
		this.onSelection = consumer;
		return this.getThis();
	}

	public void onSelection(Color oldColor) {
		if (this.onSelection != null) {
			this.onSelection.consume(oldColor);
		}

	}

	public boolean mouseClick(ReadablePoint position, int button) {
		Point pos = new Point(position);
		if (this.getContainer() != null) {
			this.getContainer().convertFor(this, pos);
		}

		if (this.isEnabled() && this.isMouseHovering(pos)) {
			this.setOpened(!this.isOpened());
			return true;
		} else {
			return false;
		}
	}

	protected boolean isMouseHovering(ReadablePoint pos) {
		return pos.getX() > 0 && pos.getY() > 0 && pos.getX() < this.getLastSize().getWidth()
				&& pos.getY() < this.getLastSize().getHeight();
	}

	public Color getColor() {
		return this.color;
	}

	public boolean isOpened() {
		return this.opened;
	}

	protected class GuiPicker extends AbstractGuiElement<AbstractGuiColorPicker<T>.GuiPicker>
			implements Clickable, Draggable {
		private boolean dragging;

		protected AbstractGuiColorPicker<T>.GuiPicker getThis() {
			return this;
		}

		public int getLayer() {
			return 1;
		}

		protected ReadableDimension calcMinSize() {
			return new Dimension(100, 100);
		}

		public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
			super.draw(renderer, size, renderInfo);
			Color color = new Color();

			for (int x = 0; x < 100; ++x) {
				for (int y = 0; y < 100; ++y) {
					AbstractGuiColorPicker.this.getColorAtPosition(x, y, color);
					renderer.drawRect(x, y, 1, 1, color);
				}
			}

		}

		public boolean mouseClick(ReadablePoint position, int button) {
			if (this.isEnabled()) {
				Point pos = new Point(position);
				AbstractGuiColorPicker parent = AbstractGuiColorPicker.this;
				if (parent.getContainer() != null) {
					parent.getContainer().convertFor(parent, pos, 1);
				}

				pos.translate(0, -AbstractGuiColorPicker.this.getLastSize().getHeight());
				if (this.isMouseHovering(pos)) {
					Color oldColor = new Color(AbstractGuiColorPicker.this.color);
					AbstractGuiColorPicker.this.getColorAtPosition(pos.getX(), pos.getY(),
							AbstractGuiColorPicker.this.color);
					this.dragging = true;
					AbstractGuiColorPicker.this.onSelection(oldColor);
					return true;
				}
			}

			return false;
		}

		public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
			return this.dragging && this.mouseClick(position, button);
		}

		public boolean mouseRelease(ReadablePoint position, int button) {
			if (this.dragging) {
				this.dragging = false;
				return true;
			} else {
				return false;
			}
		}

		protected boolean isMouseHovering(ReadablePoint pos) {
			return pos.getX() > 0 && pos.getY() > 0 && pos.getX() < 100 && pos.getY() < 100;
		}
	}
}

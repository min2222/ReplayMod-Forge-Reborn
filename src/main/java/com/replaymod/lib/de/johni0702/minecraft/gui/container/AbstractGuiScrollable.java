package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;

public abstract class AbstractGuiScrollable<T extends AbstractGuiScrollable<T>> extends AbstractGuiContainer<T>
		implements Scrollable {
	private int offsetX, offsetY;
	private final ReadablePoint negativeOffset = new ReadablePoint() {
		@Override
		public int getX() {
			return -offsetX;
		}

		@Override
		public int getY() {
			return -offsetY;
		}

		@Override
		public void getLocation(WritablePoint dest) {
			dest.setLocation(getX(), getY());
		}
	};

	private Direction scrollDirection = Direction.VERTICAL;

	protected ReadableDimension lastRenderSize;

	public AbstractGuiScrollable() {
	}

	public AbstractGuiScrollable(GuiContainer container) {
		super(container);
	}

	@Override
	public void convertFor(GuiElement element, Point point, int relativeLayer) {
		super.convertFor(element, point, relativeLayer);
		if (relativeLayer > 0 || (point.getX() > 0 && point.getX() < lastRenderSize.getWidth() && point.getY() > 0
				&& point.getY() < lastRenderSize.getHeight())) {
			point.translate(offsetX, offsetY);
		} else {
			point.setLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
		}
	}

	@Override
	public void layout(ReadableDimension size, RenderInfo renderInfo) {
		if (size != null) {
			int width = size.getWidth();
			int height = size.getHeight();
			lastRenderSize = size;
			size = super.calcMinSize();
			size = new Dimension(Math.max(width, size.getWidth()), Math.max(height, size.getHeight()));
			renderInfo = renderInfo.offsetMouse(-offsetX, -offsetY);
		}
		super.layout(size, renderInfo);
	}

	@Override
	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		int width = size.getWidth();
		int height = size.getHeight();
		size = super.calcMinSize();
		size = new Dimension(Math.max(width, size.getWidth()), Math.max(height, size.getHeight()));
		renderInfo = renderInfo.offsetMouse(-offsetX, -offsetY);

		OffsetGuiRenderer offsetRenderer = new OffsetGuiRenderer(renderer, negativeOffset, size, renderInfo.layer == 0);
		offsetRenderer.startUsing();
		super.draw(offsetRenderer, size, renderInfo);
		offsetRenderer.stopUsing();
	}

	@Override
	public ReadableDimension calcMinSize() {
		return new Dimension(0, 0);
	}

	@Override
	public boolean scroll(ReadablePoint mousePosition, int dWheel) {
		Point mouse = new Point(mousePosition);
		if (getContainer() != null) {
			getContainer().convertFor(this, mouse);
		}
		if (mouse.getX() > 0 && mouse.getY() > 0 && mouse.getX() < lastRenderSize.getWidth()
				&& mouse.getY() < lastRenderSize.getHeight()) {
			// Reduce scrolling speed but make sure it is never rounded to 0
			dWheel = (int) Math.copySign(Math.ceil(Math.abs(dWheel) / 4.0), dWheel);
			if (scrollDirection == Direction.HORIZONTAL) {
				scrollX(dWheel);
			} else {
				scrollY(dWheel);
			}
			return true;
		}
		return false;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public T setOffsetX(int offsetX) {
		this.offsetX = offsetX;
		return getThis();
	}

	public int getOffsetY() {
		return offsetY;
	}

	public T setOffsetY(int offsetY) {
		this.offsetY = offsetY;
		return getThis();
	}

	public Direction getScrollDirection() {
		return scrollDirection;
	}

	public T setScrollDirection(Direction scrollDirection) {
		this.scrollDirection = scrollDirection;
		return getThis();
	}

	public T scrollX(int dPixel) {
		offsetX = Math.max(0, Math.min(super.calcMinSize().getWidth() - lastRenderSize.getWidth(), offsetX - dPixel));
		return getThis();
	}

	public T scrollY(int dPixel) {
		offsetY = Math.max(0, Math.min(super.calcMinSize().getHeight() - lastRenderSize.getHeight(), offsetY - dPixel));
		return getThis();
	}

	public enum Direction {
		VERTICAL, HORIZONTAL
	}
}

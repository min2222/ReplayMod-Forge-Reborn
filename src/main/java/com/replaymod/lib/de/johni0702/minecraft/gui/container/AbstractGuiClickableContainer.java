package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public abstract class AbstractGuiClickableContainer<T extends AbstractGuiClickableContainer<T>>
		extends AbstractGuiContainer<T> implements Clickable, IGuiClickable<T> {
	private Runnable onClick;

	public AbstractGuiClickableContainer() {
	}

	public AbstractGuiClickableContainer(GuiContainer container) {
		super(container);
	}

	public boolean mouseClick(ReadablePoint position, int button) {
		Point pos = new Point(position);
		if (this.getContainer() != null) {
			this.getContainer().convertFor(this, pos);
		}

		if (this.isMouseHovering(pos) && this.isEnabled()) {
			this.onClick();
			return true;
		} else {
			return false;
		}
	}

	protected boolean isMouseHovering(ReadablePoint pos) {
		return pos.getX() > 0 && pos.getY() > 0 && pos.getX() < this.getLastSize().getWidth()
				&& pos.getY() < this.getLastSize().getHeight();
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
	}

	protected void onClick() {
		if (this.onClick != null) {
			this.onClick.run();
		}

	}

	public T onClick(Runnable onClick) {
		this.onClick = onClick;
		return this.getThis();
	}

	public Runnable getOnClick() {
		return this.onClick;
	}
}

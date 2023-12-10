package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGuiElement<T extends AbstractGuiElement<T>> implements GuiElement<T> {
	protected static final ResourceLocation TEXTURE = new ResourceLocation("jgui", "gui.png");
	private final Minecraft minecraft = MCVer.getMinecraft();
	private GuiContainer container;
	private GuiElement tooltip;
	private boolean enabled = true;
	protected Dimension minSize;
	protected Dimension maxSize;
	private ReadableDimension lastSize;

	public AbstractGuiElement() {
	}

	public AbstractGuiElement(GuiContainer container) {
		container.addElements((LayoutData) null, this);
	}

	protected abstract T getThis();

	public void layout(ReadableDimension size, RenderInfo renderInfo) {
		if (size == null) {
			if (this.getContainer() == null) {
				throw new RuntimeException("Any top containers must implement layout(null, ...) themselves!");
			} else {
				this.getContainer().layout(size, renderInfo.layer(renderInfo.layer + this.getLayer()));
			}
		} else {
			if (renderInfo.layer == 0) {
				this.lastSize = size;
			}

		}
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
	}

	public T setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this.getThis();
	}

	public T setEnabled() {
		return this.setEnabled(true);
	}

	public T setDisabled() {
		return this.setEnabled(false);
	}

	public GuiElement getTooltip(RenderInfo renderInfo) {
		if (this.tooltip != null && this.lastSize != null) {
			Point mouse = new Point(renderInfo.mouseX, renderInfo.mouseY);
			if (this.container != null) {
				this.container.convertFor(this, mouse);
			}

			if (mouse.getX() > 0 && mouse.getY() > 0 && mouse.getX() < this.lastSize.getWidth()
					&& mouse.getY() < this.lastSize.getHeight()) {
				return this.tooltip;
			}
		}

		return null;
	}

	public T setTooltip(GuiElement tooltip) {
		this.tooltip = tooltip;
		return this.getThis();
	}

	public T setContainer(GuiContainer container) {
		this.container = container;
		return this.getThis();
	}

	public T setMinSize(ReadableDimension minSize) {
		this.minSize = new Dimension(minSize);
		return this.getThis();
	}

	public T setMaxSize(ReadableDimension maxSize) {
		this.maxSize = new Dimension(maxSize);
		return this.getThis();
	}

	public T setSize(ReadableDimension size) {
		this.setMinSize(size);
		return this.setMaxSize(size);
	}

	public T setSize(int width, int height) {
		return this.setSize(new Dimension(width, height));
	}

	public T setWidth(int width) {
		if (this.minSize == null) {
			this.minSize = new Dimension(width, 0);
		} else {
			this.minSize.setWidth(width);
		}

		if (this.maxSize == null) {
			this.maxSize = new Dimension(width, Integer.MAX_VALUE);
		} else {
			this.maxSize.setWidth(width);
		}

		return this.getThis();
	}

	public T setHeight(int height) {
		if (this.minSize == null) {
			this.minSize = new Dimension(0, height);
		} else {
			this.minSize.setHeight(height);
		}

		if (this.maxSize == null) {
			this.maxSize = new Dimension(Integer.MAX_VALUE, height);
		} else {
			this.maxSize.setHeight(height);
		}

		return this.getThis();
	}

	public int getLayer() {
		return 0;
	}

	public ReadableDimension getMinSize() {
		ReadableDimension calcSize = this.calcMinSize();
		if (this.minSize == null) {
			return calcSize;
		} else {
			return this.minSize.getWidth() >= calcSize.getWidth() && this.minSize.getHeight() >= calcSize.getHeight()
					? this.minSize
					: new Dimension(Math.max(calcSize.getWidth(), this.minSize.getWidth()),
							Math.max(calcSize.getHeight(), this.minSize.getHeight()));
		}
	}

	protected abstract ReadableDimension calcMinSize();

	public ReadableDimension getMaxSize() {
		return this.maxSize == null ? new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE) : this.maxSize;
	}

	public Minecraft getMinecraft() {
		return this.minecraft;
	}

	public GuiContainer getContainer() {
		return this.container;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	protected ReadableDimension getLastSize() {
		return this.lastSize;
	}
}

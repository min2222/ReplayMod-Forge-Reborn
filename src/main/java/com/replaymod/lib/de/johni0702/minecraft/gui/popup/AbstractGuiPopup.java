package com.replaymod.lib.de.johni0702.minecraft.gui.popup;

import java.util.function.Function;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiOverlay;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

public abstract class AbstractGuiPopup<T extends AbstractGuiPopup<T>> extends AbstractGuiContainer<T> {
	private final GuiPanel popupContainer = (GuiPanel) (new GuiPanel(this) {
		private final int u0 = 0;
		private final int v0 = 39;

		public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
			if (renderInfo.getLayer() == 0 && AbstractGuiPopup.this.renderBackground) {
				renderer.bindTexture(TEXTURE);
				int w = size.getWidth();
				int h = size.getHeight();
				renderer.drawTexturedRect(0, 0, 0, 39, 5, 5);
				renderer.drawTexturedRect(w - 5, 0, 12, 39, 5, 5);
				renderer.drawTexturedRect(0, h - 5, 0, 51, 5, 5);
				renderer.drawTexturedRect(w - 5, h - 5, 12, 51, 5, 5);

				int x;
				int y;
				for (x = 5; x < w - 5; x += 5) {
					y = Math.min(5, w - 5 - x);
					renderer.drawTexturedRect(x, 0, 6, 39, y, 5);
					renderer.drawTexturedRect(x, h - 5, 6, 51, y, 5);
				}

				for (x = 5; x < h - 5; x += 5) {
					y = Math.min(5, h - 5 - x);
					renderer.drawTexturedRect(0, x, 0, 45, 5, y);
					renderer.drawTexturedRect(w - 5, x, 12, 45, 5, y);
				}

				for (x = 5; x < w - 5; x += 5) {
					for (y = 5; y < h - 5; y += 5) {
						int rx = Math.min(5, w - 5 - x);
						int ry = Math.min(5, h - 5 - y);
						renderer.drawTexturedRect(x, y, 6, 45, rx, ry);
					}
				}
			}

			super.draw(renderer, size, renderInfo);
		}
	}).setLayout(new CustomLayout<GuiPanel>() {
		protected void layout(GuiPanel container, int width, int height) {
			this.pos(AbstractGuiPopup.this.popup, 10, 10);
		}

		public ReadableDimension calcMinSize(GuiContainer<?> container) {
			ReadableDimension size = AbstractGuiPopup.this.popup.calcMinSize();
			return new Dimension(size.getWidth() + 20, size.getHeight() + 20);
		}
	});
	protected final GuiPanel popup;
	private int layer;
	private Layout originalLayout;
	private boolean wasAllowUserInput;
	private boolean wasMouseVisible;
	private boolean renderBackground;
	private final GuiContainer container;

	public AbstractGuiPopup(GuiContainer container) {
		this.popup = new GuiPanel(this.popupContainer);
		this.setLayout(new CustomLayout<T>() {
			protected void layout(T container, int width, int height) {
				this.pos(AbstractGuiPopup.this.popupContainer,
						width / 2 - this.width(AbstractGuiPopup.this.popupContainer) / 2,
						height / 2 - this.height(AbstractGuiPopup.this.popupContainer) / 2);
			}
		});

		for (this.renderBackground = true; container.getContainer() != null; container = container.getContainer()) {
		}

		this.container = container;
	}

	protected void disablePopupBackground() {
		this.renderBackground = false;
	}

	protected void open() {
		this.setLayer(this.container.getMaxLayer() + 1);
		this.container.addElements((LayoutData) null, this);
		this.container.setLayout(new CustomLayout(this.originalLayout = this.container.getLayout()) {
			protected void layout(GuiContainer container, int width, int height) {
				this.pos(AbstractGuiPopup.this, 0, 0);
				this.size(AbstractGuiPopup.this, width, height);
			}
		});
		if (this.container instanceof AbstractGuiOverlay) {
			AbstractGuiOverlay overlay = (AbstractGuiOverlay) this.container;
			this.wasAllowUserInput = overlay.isAllowUserInput();
			overlay.setAllowUserInput(false);
			this.wasMouseVisible = overlay.isMouseVisible();
			overlay.setMouseVisible(true);
		}

	}

	protected void close() {
		this.getContainer().setLayout(this.originalLayout);
		this.getContainer().removeElement(this);
		if (this.container instanceof AbstractGuiOverlay) {
			AbstractGuiOverlay overlay = (AbstractGuiOverlay) this.container;
			overlay.setAllowUserInput(this.wasAllowUserInput);
			overlay.setMouseVisible(this.wasMouseVisible);
		}

	}

	public T setLayer(int layer) {
		this.layer = layer;
		return this.getThis();
	}

	public int getLayer() {
		return this.layer;
	}

	public <C> boolean invokeHandlers(int layer, Class<C> ofType, Function<C, Boolean> handle) {
		return super.invokeHandlers(layer, ofType, handle) || layer <= 0;
	}
}

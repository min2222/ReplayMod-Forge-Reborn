package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.MinecraftGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Loadable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.MouseUtils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.ScreenExt;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.RenderHudCallback;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public abstract class AbstractGuiOverlay<T extends AbstractGuiOverlay<T>> extends AbstractGuiContainer<T> {
	private final AbstractGuiOverlay<T>.UserInputGuiScreen userInputGuiScreen = new AbstractGuiOverlay.UserInputGuiScreen();
	private final AbstractGuiOverlay<T>.EventHandler eventHandler = new AbstractGuiOverlay.EventHandler();
	private boolean visible;
	private Dimension screenSize;
	private boolean mouseVisible;
	private boolean closeable = true;

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean visible) {
		if (this.visible != visible) {
			if (visible) {
				this.invokeAll(Loadable.class, Loadable::load);
				this.eventHandler.register();
			} else {
				this.invokeAll(Closeable.class, Closeable::close);
				this.eventHandler.unregister();
			}

			this.updateUserInputGui();
		}

		this.visible = visible;
	}

	public boolean isMouseVisible() {
		return this.mouseVisible;
	}

	public void setMouseVisible(boolean mouseVisible) {
		this.mouseVisible = mouseVisible;
		this.updateUserInputGui();
	}

	public boolean isCloseable() {
		return this.closeable;
	}

	public void setCloseable(boolean closeable) {
		this.closeable = closeable;
	}

	public boolean isAllowUserInput() {
		return ((ScreenExt) this.userInputGuiScreen).doesPassEvents();
	}

	public void setAllowUserInput(boolean allowUserInput) {
		((ScreenExt) this.userInputGuiScreen).setPassEvents(allowUserInput);
	}

	private void updateUserInputGui() {
		Minecraft mc = this.getMinecraft();
		if (this.visible) {
			if (this.mouseVisible) {
				if (mc.screen == null) {
					mc.setScreen(this.userInputGuiScreen);
				}
			} else if (mc.screen == this.userInputGuiScreen) {
				mc.setScreen((Screen) null);
			}
		}

	}

	public void layout(ReadableDimension size, RenderInfo renderInfo) {
		if (size == null) {
			size = this.screenSize;
		}

		super.layout((ReadableDimension) size, renderInfo);
		if (this.mouseVisible && renderInfo.layer == this.getMaxLayer()) {
			GuiElement tooltip = (GuiElement) this.forEach(GuiElement.class, (e) -> {
				return e.getTooltip(renderInfo);
			});
			if (tooltip != null) {
				tooltip.layout(tooltip.getMinSize(), renderInfo);
			}
		}

	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		if (this.mouseVisible && renderInfo.layer == this.getMaxLayer()) {
			GuiElement tooltip = (GuiElement) this.forEach(GuiElement.class, (e) -> {
				return e.getTooltip(renderInfo);
			});
			if (tooltip != null) {
				ReadableDimension tooltipSize = tooltip.getMinSize();
				int x;
				if (renderInfo.mouseX + 8 + tooltipSize.getWidth() < this.screenSize.getWidth()) {
					x = renderInfo.mouseX + 8;
				} else {
					x = this.screenSize.getWidth() - tooltipSize.getWidth() - 1;
				}

				int y;
				if (renderInfo.mouseY + 8 + tooltipSize.getHeight() < this.screenSize.getHeight()) {
					y = renderInfo.mouseY + 8;
				} else {
					y = this.screenSize.getHeight() - tooltipSize.getHeight() - 1;
				}

				Point position = new Point(x, y);

				try {
					OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, position, tooltipSize);
					tooltip.draw(eRenderer, tooltipSize, renderInfo);
				} catch (Exception var12) {
					CrashReport crashReport = CrashReport.forThrowable(var12, "Rendering Gui Tooltip");
					renderInfo.addTo(crashReport);
					CrashReportCategory category = crashReport.addCategory("Gui container details");
					MCVer.addDetail(category, "Container", this::toString);
					MCVer.addDetail(category, "Width", () -> "" + size.getWidth());
					MCVer.addDetail(category, "Height", () -> "" + size.getHeight());
					category = crashReport.addCategory("Tooltip details");
					MCVer.addDetail(category, "Element", tooltip::toString);
					MCVer.addDetail(category, "Position", position::toString);
					MCVer.addDetail(category, "Size", tooltipSize::toString);
					throw new ReportedException(crashReport);
				}
			}
		}

	}

	public ReadableDimension getMinSize() {
		return this.screenSize;
	}

	public ReadableDimension getMaxSize() {
		return this.screenSize;
	}

	protected class UserInputGuiScreen extends Screen {
		UserInputGuiScreen() {
			super(MCVer.literalText(""));
			((ScreenExt) this).setPassEvents(true);
		}

		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			Point mousePos = MouseUtils.getMousePos();
			boolean controlDown = hasControlDown();
			boolean shiftDown = hasShiftDown();
			return !AbstractGuiOverlay.this.invokeHandlers(Typeable.class, (e) -> {
				return e.typeKey(mousePos, keyCode, '\u0000', controlDown, shiftDown);
			}) ? super.keyPressed(keyCode, scanCode, modifiers) : true;
		}

		public boolean charTyped(char keyChar, int modifiers) {
			Point mousePos = MouseUtils.getMousePos();
			boolean controlDown = hasControlDown();
			boolean shiftDown = hasShiftDown();
			return !AbstractGuiOverlay.this.invokeHandlers(Typeable.class, (e) -> {
				return e.typeKey(mousePos, 0, keyChar, controlDown, shiftDown);
			}) ? super.charTyped(keyChar, modifiers) : true;
		}

		public boolean mouseClicked(double mouseXD, double mouseYD, int mouseButton) {
			int mouseX = (int) Math.round(mouseXD);
			int mouseY = (int) Math.round(mouseYD);
			return AbstractGuiOverlay.this.invokeHandlers(Clickable.class, (e) -> {
				return e.mouseClick(new Point(mouseX, mouseY), mouseButton);
			});
		}

		public boolean mouseReleased(double mouseXD, double mouseYD, int mouseButton) {
			int mouseX = (int) Math.round(mouseXD);
			int mouseY = (int) Math.round(mouseYD);
			return AbstractGuiOverlay.this.invokeHandlers(Draggable.class, (e) -> {
				return e.mouseRelease(new Point(mouseX, mouseY), mouseButton);
			});
		}

		public boolean mouseDragged(double mouseXD, double mouseYD, int mouseButton, double deltaX, double deltaY) {
			int mouseX = (int) Math.round(mouseXD);
			int mouseY = (int) Math.round(mouseYD);
			long timeSinceLastClick = 0L;
			return AbstractGuiOverlay.this.invokeHandlers(Draggable.class, (e) -> {
				return e.mouseDrag(new Point(mouseX, mouseY), mouseButton, timeSinceLastClick);
			});
		}

		public void tick() {
			AbstractGuiOverlay.this.invokeAll(Tickable.class, Tickable::tick);
		}

		public boolean mouseScrolled(double mouseX, double mouseY, double dWheel) {
			Point mouse = new Point((int) mouseX, (int) mouseY);
			int wheel = (int) (dWheel * 120.0D);
			return AbstractGuiOverlay.this.invokeHandlers(Scrollable.class, (e) -> {
				return e.scroll(mouse, wheel);
			});
		}

		public void onClose() {
			if (AbstractGuiOverlay.this.closeable) {
				super.onClose();
			}

		}

		public void removed() {
			if (AbstractGuiOverlay.this.closeable) {
				AbstractGuiOverlay.this.mouseVisible = false;
			}

		}

		public AbstractGuiOverlay<T> getOverlay() {
			return AbstractGuiOverlay.this;
		}
	}

	private class EventHandler extends EventRegistrations {
		private EventHandler() {
			this.on(RenderHudCallback.EVENT, this::renderOverlay);
			this.on(PreTickCallback.EVENT, () -> {
				AbstractGuiOverlay.this.invokeAll(Tickable.class, Tickable::tick);
			});
		}

		private void renderOverlay(PoseStack stack, float partialTicks) {
			AbstractGuiOverlay.this.updateUserInputGui();
			this.updateRenderer();
			int layers = AbstractGuiOverlay.this.getMaxLayer();
			int mouseX = -1;
			int mouseY = -1;
			if (AbstractGuiOverlay.this.mouseVisible) {
				Point mouse = MouseUtils.getMousePos();
				mouseX = mouse.getX();
				mouseY = mouse.getY();
			}

			RenderInfo renderInfo = new RenderInfo(partialTicks, mouseX, mouseY, 0);

			for (int layer = 0; layer <= layers; ++layer) {
				AbstractGuiOverlay.this.layout(AbstractGuiOverlay.this.screenSize, renderInfo.layer(layer));
			}

			MinecraftGuiRenderer renderer = new MinecraftGuiRenderer(stack);

			for (int layerx = 0; layerx <= layers; ++layerx) {
				AbstractGuiOverlay.this.draw(renderer, AbstractGuiOverlay.this.screenSize, renderInfo.layer(layerx));
			}

		}

		private void updateRenderer() {
			Minecraft mc = AbstractGuiOverlay.this.getMinecraft();
			Window res = MCVer.newScaledResolution(mc);
			if (AbstractGuiOverlay.this.screenSize == null
					|| AbstractGuiOverlay.this.screenSize.getWidth() != res.getGuiScaledWidth()
					|| AbstractGuiOverlay.this.screenSize.getHeight() != res.getGuiScaledHeight()) {
				AbstractGuiOverlay.this.screenSize = new Dimension(res.getGuiScaledWidth(), res.getGuiScaledHeight());
			}

		}
	}
}

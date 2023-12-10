package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.MouseUtils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.OpenGuiScreenCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PostRenderScreenCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VanillaGuiScreen extends GuiScreen implements Draggable, Typeable, Scrollable, Tickable {
	private static final Map<Screen, VanillaGuiScreen> WRAPPERS = Collections.synchronizedMap(new WeakHashMap());
	private final Screen mcScreen;
	private final VanillaGuiScreen.EventHandler eventHandler = new VanillaGuiScreen.EventHandler();

	public static VanillaGuiScreen wrap(Screen originalGuiScreen) {
		VanillaGuiScreen gui = (VanillaGuiScreen) WRAPPERS.get(originalGuiScreen);
		if (gui == null) {
			WRAPPERS.put(originalGuiScreen, gui = new VanillaGuiScreen(originalGuiScreen));
			gui.register();
		}

		return gui;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static VanillaGuiScreen setup(Screen originalGuiScreen) {
		VanillaGuiScreen gui = new VanillaGuiScreen(originalGuiScreen);
		gui.register();
		return gui;
	}

	public VanillaGuiScreen(Screen mcScreen) {
		this.mcScreen = mcScreen;
		this.suppressVanillaKeys = true;
		super.setBackground(AbstractGuiScreen.Background.NONE);
	}

	public void register() {
		if (!this.eventHandler.active) {
			this.eventHandler.active = true;
			this.eventHandler.register();
			this.getSuperMcGui().init(MCVer.getMinecraft(), this.mcScreen.width, this.mcScreen.height);
		}

	}

	public void display() {
		this.getMinecraft().setScreen(this.mcScreen);
		this.register();
	}

	public Screen toMinecraft() {
		return this.mcScreen;
	}

	public void setBackground(AbstractGuiScreen.Background background) {
		throw new UnsupportedOperationException("Cannot set background of vanilla gui screen.");
	}

	private Screen getSuperMcGui() {
		return super.toMinecraft();
	}

	public boolean mouseClick(ReadablePoint position, int button) {
		return false;
	}

	public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
		return false;
	}

	public boolean mouseRelease(ReadablePoint position, int button) {
		return false;
	}

	public boolean scroll(ReadablePoint mousePosition, int dWheel) {
		return false;
	}

	public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
			boolean shiftDown) {
		return false;
	}

	public void tick() {
		if (this.getSuperMcGui() == this.getMinecraft().screen) {
			this.getMinecraft().setScreen((Screen) null);
		}

	}

	private class EventHandler extends EventRegistrations {
		private boolean active;

		{
			on(OpenGuiScreenCallback.EVENT, screen -> onGuiClosed());
		}

		private void onGuiClosed() {
			unregister();

			if (active) {
				active = false;
				getSuperMcGui().removed();
				WRAPPERS.remove(mcScreen, VanillaGuiScreen.this);
			}
		}

		{
			on(InitScreenCallback.Pre.EVENT, this::preGuiInit);
		}

		private void preGuiInit(Screen screen) {
			if (screen == mcScreen && active) {
				active = false;
				unregister();
				getSuperMcGui().removed();
				WRAPPERS.remove(mcScreen, VanillaGuiScreen.this);
			}
		}

		{
			on(PostRenderScreenCallback.EVENT, this::onGuiRender);
		}

		private void onGuiRender(PoseStack stack, float partialTicks) {
			Point mousePos = MouseUtils.getMousePos();
			getSuperMcGui().render(stack, mousePos.getX(), mousePos.getY(), partialTicks);
		}

		{
			on(PreTickCallback.EVENT, this::tickOverlay);
		}

		private void tickOverlay() {
			getSuperMcGui().tick();
		}

		private boolean handled;

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void mouseClicked(ScreenEvent.MouseButtonPressed event) {
			handled = getSuperMcGui().mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
			if (handled) {
				event.setCanceled(true);
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void mouseDrag(ScreenEvent.MouseDragged event) {
			handled = getSuperMcGui().mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton(),
					event.getDragX(), event.getDragY());
			if (handled) {
				event.setCanceled(true);
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void mouseClicked(ScreenEvent.MouseButtonReleased event) {
			handled = getSuperMcGui().mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
			if (handled) {
				event.setCanceled(true);
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void mouseClicked(ScreenEvent.MouseScrolled event) {
			handled = getSuperMcGui().mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
			if (handled) {
				event.setCanceled(true);
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void mouseClicked(ScreenEvent.KeyPressed event) {
			handled = getSuperMcGui().keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers());
			if (handled) {
				event.setCanceled(true);
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void mouseClicked(ScreenEvent.KeyReleased event) {
			handled = getSuperMcGui().keyReleased(event.getKeyCode(), event.getScanCode(), event.getModifiers());
			if (handled) {
				event.setCanceled(true);
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void mouseClicked(ScreenEvent.CharacterTyped event) {
			handled = getSuperMcGui().charTyped(event.getCodePoint(), event.getModifiers());
			if (handled) {
				event.setCanceled(true);
			}
		}
	}
}

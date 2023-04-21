package com.replaymod.gui.container;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.gui.function.Draggable;
import com.replaymod.gui.function.Scrollable;
import com.replaymod.gui.function.Typeable;
import com.replaymod.gui.utils.EventRegistrations;
import com.replaymod.gui.utils.MouseUtils;
import com.replaymod.gui.versions.MCVer;
import com.replaymod.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.gui.versions.callbacks.OpenGuiScreenCallback;
import com.replaymod.gui.versions.callbacks.PostRenderScreenCallback;
import com.replaymod.gui.versions.callbacks.PreTickCallback;

import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class VanillaGuiScreen extends GuiScreen implements Draggable, Typeable, Scrollable {

    private static final Map<Screen, VanillaGuiScreen> WRAPPERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static VanillaGuiScreen wrap(Screen originalGuiScreen) {
        VanillaGuiScreen gui = WRAPPERS.get(originalGuiScreen);
        if (gui == null) {
            WRAPPERS.put(originalGuiScreen, gui = new VanillaGuiScreen(originalGuiScreen));
            gui.register();
        }
        return gui;
    }

    // Use wrap instead and make sure to preserve the existing layout.
    // (or if you really want your own, inline this code)
    @Deprecated
    public static VanillaGuiScreen setup(Screen originalGuiScreen) {
        VanillaGuiScreen gui = new VanillaGuiScreen(originalGuiScreen);
        gui.register();
        return gui;
    }

    private final Screen mcScreen;
    private final EventHandler eventHandler = new EventHandler();

    public VanillaGuiScreen(Screen mcScreen) {
        this.mcScreen = mcScreen;
        this.suppressVanillaKeys = true;

        super.setBackground(Background.NONE);
    }

    // Needs to be called from or after GuiInitEvent.Post, will auto-unregister on any GuiOpenEvent
    public void register() {
        if (!eventHandler.active) {
            eventHandler.active = true;

            eventHandler.register();

            getSuperMcGui().init(MCVer.getMinecraft(), mcScreen.width, mcScreen.height);
        }
    }

    public void display() {
        getMinecraft().setScreen(mcScreen);
        register();
    }

    @Override
    public Screen toMinecraft() {
        return mcScreen;
    }

    @Override
    public void setBackground(Background background) {
        throw new UnsupportedOperationException("Cannot set background of vanilla gui screen.");
    }

    private Screen getSuperMcGui() {
        return super.toMinecraft();
    }

    @Override
    public boolean mouseClick(ReadablePoint position, int button) {
        return false;
    }

    @Override
    public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
        return false;
    }

    @Override
    public boolean mouseRelease(ReadablePoint position, int button) {
        return false;
    }

    @Override
    public boolean scroll(ReadablePoint mousePosition, int dWheel) {
        return false;
    }

    @Override
    public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
        return false;
    }

    // Used when wrapping an already existing mc.GuiScreen
    private
    class EventHandler extends EventRegistrations {
        private boolean active;

        {
            on(OpenGuiScreenCallback.EVENT, screen -> onGuiClosed());
        }

        private void onGuiClosed() {
            unregister();

            if (active) {
                active = false;
                getSuperMcGui().onClose();
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
                getSuperMcGui().onClose();
                WRAPPERS.remove(mcScreen, VanillaGuiScreen.this);
            }
        }

        {
            on(PostRenderScreenCallback.EVENT, this::onGuiRender);
        }

        private void onGuiRender(PoseStack stack, float partialTicks) {
            Point mousePos = MouseUtils.getMousePos();
            getSuperMcGui().render(
                    stack,
                    mousePos.getX(), mousePos.getY(), partialTicks);
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
            handled = getSuperMcGui().mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton(), event.getDragX(), event.getDragY());
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
        public void mouseClicked(ScreenEvent.CharacterTyped	 event) {
            handled = getSuperMcGui().charTyped(event.getCodePoint(), event.getModifiers());
            if (handled) {
                event.setCanceled(true);
            }
        }
    }
}

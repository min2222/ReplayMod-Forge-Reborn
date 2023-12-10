package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.MinecraftGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Loadable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.MouseUtils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractGuiScreen<T extends AbstractGuiScreen<T>> extends AbstractGuiContainer<T> {

    private final MinecraftGuiScreen wrapped = new MinecraftGuiScreen();

    private Dimension screenSize;

    private Background background = Background.DEFAULT;

    private boolean enabledRepeatedKeyEvents = true;

    private GuiLabel title;

    protected boolean suppressVanillaKeys;

    public Screen toMinecraft() {
        return wrapped;
    }

    @Override
    public void layout(ReadableDimension size, RenderInfo renderInfo) {
        if (size == null) {
            size = screenSize;
        }
        if (renderInfo.layer == 0) {
            if (title != null) {
                title.layout(title.getMinSize(), renderInfo);
            }
        }
        super.layout(size, renderInfo);
        if (renderInfo.layer == getMaxLayer()) {
            final GuiElement tooltip = forEach(GuiElement.class, e -> e.getTooltip(renderInfo));
            if (tooltip != null) {
                tooltip.layout(tooltip.getMinSize(), renderInfo);
            }
        }
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.layer == 0) {
            switch (background) {
                case NONE:
                    break;
                case DEFAULT:
                    wrapped.renderBackground(renderer.getMatrixStack());
                    break;
                case TRANSPARENT:
                    int top = 0xc0_10_10_10, bottom = 0xd0_10_10_10;
                    renderer.drawRect(0, 0, size.getWidth(), size.getHeight(), top, top, bottom, bottom);
                    break;
                case DIRT:
                    wrapped.renderDirtBackground(0);
                    break;
            }
            if (title != null) {
                ReadableDimension titleSize = title.getMinSize();
                int x = screenSize.getWidth() / 2 - titleSize.getWidth() / 2;
                OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, new Point(x, 10), new Dimension(0, 0));
                title.draw(eRenderer, titleSize, renderInfo);
            }
        }
        super.draw(renderer, size, renderInfo);
        if (renderInfo.layer == getMaxLayer()) {
            final GuiElement tooltip = forEach(GuiElement.class, e -> e.getTooltip(renderInfo));
            if (tooltip != null) {
                final ReadableDimension tooltipSize = tooltip.getMinSize();
                int x, y;
                if (renderInfo.mouseX + 8 + tooltipSize.getWidth() < screenSize.getWidth()) {
                    x = renderInfo.mouseX + 8;
                } else {
                    x = screenSize.getWidth() - tooltipSize.getWidth() - 1;
                }
                if (renderInfo.mouseY + 8 + tooltipSize.getHeight() < screenSize.getHeight()) {
                    y = renderInfo.mouseY + 8;
                } else {
                    y = screenSize.getHeight() - tooltipSize.getHeight() - 1;
                }
                final ReadablePoint position = new Point(x, y);
                try {
                    OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, position, tooltipSize);
                    tooltip.draw(eRenderer, tooltipSize, renderInfo);
                } catch (Exception ex) {
                    CrashReport crashReport = CrashReport.forThrowable(ex, "Rendering Gui Tooltip");
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

    @Override
    public ReadableDimension getMinSize() {
        return screenSize;
    }

    @Override
    public ReadableDimension getMaxSize() {
        return screenSize;
    }

    public void setEnabledRepeatedKeyEvents(boolean enableRepeatKeyEvents) {
        this.enabledRepeatedKeyEvents = enableRepeatKeyEvents;
        if (wrapped.active) {
            MCVer.Keyboard.enableRepeatEvents(enableRepeatKeyEvents);
        }
    }

    public void display() {
        getMinecraft().setScreen(toMinecraft());
    }

    public Background getBackground() {
        return this.background;
    }

    public boolean isEnabledRepeatedKeyEvents() {
        return this.enabledRepeatedKeyEvents;
    }

    public GuiLabel getTitle() {
        return this.title;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public void setTitle(GuiLabel title) {
        this.title = title;
    }

    protected class MinecraftGuiScreen extends Screen {
        private boolean active;

        protected MinecraftGuiScreen() {
            super(Component.literal(""));
        }

        @Override
        public Component getNarrationMessage() {
            return title == null ? Component.literal("") : title;
        }

        @Override
        public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
            // The Forge loading screen apparently leaves one of the textures of the GlStateManager in an
            // incorrect state which can cause the whole screen to just remain white. This is a workaround.

            int layers = getMaxLayer();
            RenderInfo renderInfo = new RenderInfo(partialTicks, mouseX, mouseY, 0);
            for (int layer = 0; layer <= layers; layer++) {
                layout(screenSize, renderInfo.layer(layer));
            }
            MinecraftGuiRenderer renderer = new MinecraftGuiRenderer(stack);
            for (int layer = 0; layer <= layers; layer++) {
                draw(renderer, screenSize, renderInfo.layer(layer));
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            Point mouse = MouseUtils.getMousePos();
            boolean ctrlDown = hasControlDown();
            boolean shiftDown = hasShiftDown();
            if (!invokeHandlers(Typeable.class, e -> e.typeKey(mouse, keyCode, '\0', ctrlDown, shiftDown))) {
                if (suppressVanillaKeys) {
                    return false;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;
        }

        @Override
        public boolean charTyped(char keyChar, int scanCode) {
            Point mouse = MouseUtils.getMousePos();
            boolean ctrlDown = hasControlDown();
            boolean shiftDown = hasShiftDown();
            if (!invokeHandlers(Typeable.class, e -> e.typeKey(mouse, 0, keyChar, ctrlDown, shiftDown))) {
                if (suppressVanillaKeys) {
                    return false;
                }
                return super.charTyped(keyChar, scanCode);
            }
            return true;
        }

        @Override
        public boolean mouseClicked(double mouseXD, double mouseYD, int mouseButton) {
            int mouseX = (int) Math.round(mouseXD), mouseY = (int) Math.round(mouseYD);
            return
                    invokeHandlers(Clickable.class, e -> e.mouseClick(new Point(mouseX, mouseY), mouseButton));
        }

        @Override
        public boolean mouseReleased(double mouseXD, double mouseYD, int mouseButton) {
            int mouseX = (int) Math.round(mouseXD), mouseY = (int) Math.round(mouseYD);
            return
                    invokeHandlers(Draggable.class, e -> e.mouseRelease(new Point(mouseX, mouseY), mouseButton));
        }

        @Override
        public boolean mouseDragged(double mouseXD, double mouseYD, int mouseButton, double deltaX, double deltaY) {
            int mouseX = (int) Math.round(mouseXD), mouseY = (int) Math.round(mouseYD);
            long timeSinceLastClick = 0;
            return
                    invokeHandlers(Draggable.class, e -> e.mouseDrag(new Point(mouseX, mouseY), mouseButton, timeSinceLastClick));
        }

        @Override
        public void tick() {
            invokeAll(Tickable.class, Tickable::tick);
        }

        @Override
        public boolean mouseScrolled(
                double mouseX,
                double mouseY,
                double dWheel
        ) {
            Point mouse = new Point((int) mouseX, (int) mouseY);
            int wheel = (int) (dWheel * 120);
            return invokeHandlers(Scrollable.class, e -> e.scroll(mouse, wheel));
        }

        @Override
        public void removed() {
            invokeAll(Closeable.class, Closeable::close);
            active = false;
            if (enabledRepeatedKeyEvents) {
                MCVer.Keyboard.enableRepeatEvents(false);
            }
        }

        @Override
        public void init() {
            active = false;
            if (enabledRepeatedKeyEvents) {
                MCVer.Keyboard.enableRepeatEvents(true);
            }
            screenSize = new Dimension(width, height);
            invokeAll(Loadable.class, Loadable::load);
        }

        public T getWrapper() {
            return AbstractGuiScreen.this.getThis();
        }
    }

    public enum Background {
        NONE, DEFAULT, TRANSPARENT, DIRT;
    }
}

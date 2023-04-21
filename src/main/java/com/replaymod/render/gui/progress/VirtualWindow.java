package com.replaymod.render.gui.progress;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.replaymod.gui.function.Closeable;
import com.replaymod.mixin.MainWindowAccessor;

import net.minecraft.client.Minecraft;

public class VirtualWindow implements Closeable {
    private final Minecraft mc;
    private final Window window;
    private final MainWindowAccessor acc;

    private final RenderTarget guiFramebuffer;
    private boolean isBound;
    private int framebufferWidth, framebufferHeight;

    private int gameWidth, gameHeight;


    public VirtualWindow(Minecraft mc) {
        this.mc = mc;
        this.window = mc.getWindow();
        this.acc = (MainWindowAccessor) (Object) this.window;

        framebufferWidth = acc.getFramebufferWidth();
        framebufferHeight = acc.getFramebufferHeight();

        //#if MC>=11700
        //$$ guiFramebuffer = new WindowFramebuffer(framebufferWidth, framebufferHeight);
        //#else
        guiFramebuffer = new TextureTarget(framebufferWidth, framebufferHeight, true
                //#if MC>=11400
                , false
                //#endif
        );
        //#endif

        //TODO
        //MinecraftClientExt.get(mc).setWindowDelegate(this);
    }

    @Override
    public void close() {
        guiFramebuffer.destroyBuffers();
        //TODO
        //MinecraftClientExt.get(mc).setWindowDelegate(null);
    }

    public void bind() {
        gameWidth = acc.getFramebufferWidth();
        gameHeight = acc.getFramebufferHeight();
        acc.setFramebufferWidth(framebufferWidth);
        acc.setFramebufferHeight(framebufferHeight);
        applyScaleFactor();
        isBound = true;
    }

    public void unbind() {
        acc.setFramebufferWidth(gameWidth);
        acc.setFramebufferHeight(gameHeight);
        applyScaleFactor();
        isBound = false;
    }

    public void beginWrite() {
        guiFramebuffer.bindWrite(true);
    }

    public void endWrite() {
        guiFramebuffer.unbindWrite();
    }

    public void flip() {
        guiFramebuffer.blitToScreen(framebufferWidth, framebufferHeight);

        window.updateDisplay();
    }

    /**
     * Updates the size of the window's framebuffer. Must only be called while this window is bound.
     */
    public void onResolutionChanged(int newWidth, int newHeight) {
        if (newWidth == 0 || newHeight == 0) {
            // These can be zero on Windows if minimized.
            // Creating zero-sized framebuffers however will throw an error, so we never want to switch to zero values.
            return;
        }

        if (framebufferWidth == newWidth && framebufferHeight == newHeight) {
            return; // size is unchanged, nothing to do
        }

        framebufferWidth = newWidth;
        framebufferHeight = newHeight;

        //#if MC>=11400
        guiFramebuffer.resize(newWidth, newHeight
                //#if MC>=11400
                , false
                //#endif
        );
        applyScaleFactor();
        if (mc.screen != null) {
            mc.screen.resize(mc, window.getGuiScaledWidth(), window.getGuiScaledHeight());
        }
    }

    private void applyScaleFactor() {
        window.setGuiScale(window.calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode()));
    }

    public int getFramebufferWidth() {
        return framebufferWidth;
    }

    public int getFramebufferHeight() {
        return framebufferHeight;
    }

    public boolean isBound() {
        return isBound;
    }
}
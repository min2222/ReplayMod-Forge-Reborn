package com.replaymod.replay;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.core.ReplayMod;
import com.replaymod.gui.versions.Image;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;


public class NoGuiScreenshot {
    private final Image image;
    private final int width;
    private final int height;

    private NoGuiScreenshot(Image image, int width, int height) {
        this.image = image;
        this.width = width;
        this.height = height;
    }

    public Image getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static ListenableFuture<NoGuiScreenshot> take(final Minecraft mc, final int width, final int height) {
        final SettableFuture<NoGuiScreenshot> future = SettableFuture.create();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (future.isCancelled()) {
                    return;
                }

                int frameWidth = mc.getWindow().getWidth();
                int frameHeight = mc.getWindow().getHeight();

                final boolean guiHidden = mc.options.hideGui;
                try {
                    mc.options.hideGui = true;

                    PoseStack stack = RenderSystem.getModelViewStack();
                    // Render frame without GUI
                    stack.pushPose();
                    RenderSystem.clear(
                            16640
                            , true
                    );
                    mc.getMainRenderTarget().bindWrite(true);
                    RenderSystem.enableTexture();

                    float tickDelta = mc.getPartialTick();
                    mc.gameRenderer.renderLevel(tickDelta, System.nanoTime(), new PoseStack());

                    mc.getMainRenderTarget().unbindWrite();
                    stack.popPose();
                    stack.pushPose();
                    mc.getMainRenderTarget().blitToScreen(frameWidth, frameHeight);
                    stack.popPose();
                } catch (Throwable t) {
                    future.setException(t);
                    return;
                } finally {
                    // Reset GUI settings
                    mc.options.hideGui = guiHidden;
                }

                // The frame without GUI has been rendered
                // Read it, create the screenshot and finish the future
                try {
                    Image image = new Image(Screenshot.takeScreenshot(mc.getMainRenderTarget()));
                    int imageWidth = image.getWidth();
                    int imageHeight = image.getHeight();

                    // Scale & crop
                    float scaleFactor = Math.max((float) width / imageWidth, (float) height / imageHeight);
                    int croppedWidth = Math.min(Math.max(0, (int) (width / scaleFactor)), imageWidth);
                    int croppedHeight = Math.min(Math.max(0, (int) (height / scaleFactor)), imageHeight);
                    int offsetX = (imageWidth - croppedWidth) / 2;
                    int offsetY = (imageHeight - croppedHeight) / 2;
                    image = image.scaledSubRect(offsetX, offsetY, croppedWidth, croppedHeight, width, height);

                    // Finish
                    future.set(new NoGuiScreenshot(image, width, height));
                } catch (Throwable t) {
                    future.setException(t);
                }
            }
        };

        // Make sure we are not somewhere in the middle of the rendering process but always at the beginning
        // of the game loop. We cannot use the addScheduledTask method as it'll run the task if called
        // from the minecraft thread which is exactly what we want to avoid.
        ReplayMod.instance.runLater(runnable);
        return future;
    }
}

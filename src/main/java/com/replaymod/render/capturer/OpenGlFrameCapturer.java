package com.replaymod.render.capturer;

import static com.replaymod.core.versions.MCVer.resizeMainWindow;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.core.versions.MCVer;
import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.rendering.Frame;
import com.replaymod.render.rendering.FrameCapturer;
import com.replaymod.render.utils.ByteBufferPool;

import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import net.minecraft.client.Minecraft;

public abstract class OpenGlFrameCapturer<F extends Frame, D extends CaptureData> implements FrameCapturer<F> {
    protected final WorldRenderer worldRenderer;
    protected final RenderInfo renderInfo;
    protected int framesDone;
    private RenderTarget frameBuffer;

    protected final Minecraft mc = MCVer.getMinecraft();

    public OpenGlFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo) {
        this.worldRenderer = worldRenderer;
        this.renderInfo = renderInfo;
    }

    protected final ReadableDimension frameSize = new ReadableDimension() {
        @Override
        public int getWidth() {
            return getFrameWidth();
        }

        @Override
        public int getHeight() {
            return getFrameHeight();
        }

        @Override
        public void getSize(WritableDimension dest) {
            dest.setSize(getWidth(), getHeight());
        }
    };

    protected int getFrameWidth() {
        return renderInfo.getFrameSize().getWidth();
    }

    protected int getFrameHeight() {
        return renderInfo.getFrameSize().getHeight();
    }

    protected RenderTarget frameBuffer() {
        if (frameBuffer == null) {
            frameBuffer = mc.getMainRenderTarget();
        }
        return frameBuffer;
    }

    @Override
    public boolean isDone() {
        return framesDone >= renderInfo.getTotalFrames();
    }

    protected OpenGlFrame renderFrame(int frameId, float partialTicks) {
        return renderFrame(frameId, partialTicks, null);
    }

    protected OpenGlFrame renderFrame(int frameId, float partialTicks, D captureData) {
        resizeMainWindow(mc, getFrameWidth(), getFrameHeight());
        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        frameBuffer().bindWrite(true);

        RenderSystem.clear(16640, false);
        RenderSystem.enableTexture();

        worldRenderer.renderWorld(partialTicks, captureData);

        frameBuffer().unbindWrite();
        matrixStack.popPose();

        return captureFrame(frameId, captureData);
    }

    protected OpenGlFrame captureFrame(int frameId, D captureData) {
        ByteBuffer buffer = ByteBufferPool.allocate(getFrameWidth() * getFrameHeight() * 4);
        frameBuffer().bindWrite(true);
        GL11.glReadPixels(0, 0, getFrameWidth(), getFrameHeight(), 32993, 5121, buffer);
        frameBuffer().unbindWrite();
        buffer.rewind();

        return new OpenGlFrame(frameId, new Dimension(getFrameWidth(), getFrameHeight()), 4, buffer);
    }

    @Override
    public void close() throws IOException {
    }
}

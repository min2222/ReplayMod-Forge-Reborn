package com.replaymod.render.capturer;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.rendering.Frame;
import com.replaymod.render.rendering.FrameCapturer;
import com.replaymod.render.utils.ByteBufferPool;

import net.minecraft.client.Minecraft;

public abstract class OpenGlFrameCapturer<F extends Frame, D extends CaptureData> implements FrameCapturer<F> {
	protected final WorldRenderer worldRenderer;
	protected final RenderInfo renderInfo;
	protected int framesDone;
	private RenderTarget frameBuffer;
	protected final Minecraft mc = MCVer.getMinecraft();
	protected final ReadableDimension frameSize = new ReadableDimension() {
		public int getWidth() {
			return OpenGlFrameCapturer.this.getFrameWidth();
		}

		public int getHeight() {
			return OpenGlFrameCapturer.this.getFrameHeight();
		}

		public void getSize(WritableDimension dest) {
			dest.setSize(this.getWidth(), this.getHeight());
		}
	};

	public OpenGlFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo) {
		this.worldRenderer = worldRenderer;
		this.renderInfo = renderInfo;
	}

	protected int getFrameWidth() {
		return this.renderInfo.getFrameSize().getWidth();
	}

	protected int getFrameHeight() {
		return this.renderInfo.getFrameSize().getHeight();
	}

	protected RenderTarget frameBuffer() {
		if (this.frameBuffer == null) {
			this.frameBuffer = this.mc.getMainRenderTarget();
		}

		return this.frameBuffer;
	}

	public boolean isDone() {
		return this.framesDone >= this.renderInfo.getTotalFrames();
	}

	protected OpenGlFrame renderFrame(int frameId, float partialTicks) {
		return this.renderFrame(frameId, partialTicks, null);
	}

	protected OpenGlFrame renderFrame(int frameId, float partialTicks, D captureData) {
		MCVer.resizeMainWindow(this.mc, this.getFrameWidth(), this.getFrameHeight());
		MCVer.pushMatrix();
		this.frameBuffer().bindWrite(true);
		RenderSystem.clear(16640, false);
		RenderSystem.enableTexture();
		this.worldRenderer.renderWorld(partialTicks, captureData);
		this.frameBuffer().unbindWrite();
		MCVer.popMatrix();
		return this.captureFrame(frameId, captureData);
	}

	protected OpenGlFrame captureFrame(int frameId, D captureData) {
		ByteBuffer buffer = ByteBufferPool.allocate(this.getFrameWidth() * this.getFrameHeight() * 4);
		this.frameBuffer().bindWrite(true);
		GL11.glReadPixels(0, 0, this.getFrameWidth(), this.getFrameHeight(), 32993, 5121, buffer);
		this.frameBuffer().unbindWrite();
		buffer.rewind();
		return new OpenGlFrame(frameId, new Dimension(this.getFrameWidth(), this.getFrameHeight()), 4, buffer);
	}

	public void close() throws IOException {
	}
}

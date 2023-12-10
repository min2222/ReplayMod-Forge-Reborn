package com.replaymod.render.capturer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.rendering.Channel;
import com.replaymod.render.rendering.Frame;
import com.replaymod.render.utils.ByteBufferPool;
import com.replaymod.render.utils.PixelBufferObject;

public abstract class PboOpenGlFrameCapturer<F extends Frame, D extends Enum<D> & CaptureData>
		extends OpenGlFrameCapturer<F, D> {
	private final boolean withDepth;
	private final D[] data;
	private PixelBufferObject pbo;
	private PixelBufferObject otherPBO;

	public PboOpenGlFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo, Class<D> type, int framePixels) {
		super(worldRenderer, renderInfo);
		this.withDepth = renderInfo.getRenderSettings().isDepthMap();
		this.data = type.getEnumConstants();
		int bufferSize = framePixels * (4 + (this.withDepth ? 4 : 0)) * this.data.length;
		this.pbo = new PixelBufferObject((long) bufferSize, PixelBufferObject.Usage.READ);
		this.otherPBO = new PixelBufferObject((long) bufferSize, PixelBufferObject.Usage.READ);
	}

	protected abstract F create(OpenGlFrame[] openGlFrames);

	private void swapPBOs() {
		PixelBufferObject old = this.pbo;
		this.pbo = this.otherPBO;
		this.otherPBO = old;
	}

	public boolean isDone() {
		return this.framesDone >= this.renderInfo.getTotalFrames() + 2;
	}

	private F readFromPbo(ByteBuffer pboBuffer, int bytesPerPixel) {
		OpenGlFrame[] frames = new OpenGlFrame[this.data.length];
		int frameBufferSize = this.getFrameWidth() * this.getFrameHeight() * bytesPerPixel;

		for (int i = 0; i < frames.length; ++i) {
			ByteBuffer frameBuffer = ByteBufferPool.allocate(frameBufferSize);
			pboBuffer.limit(pboBuffer.position() + frameBufferSize);
			frameBuffer.put(pboBuffer);
			frameBuffer.rewind();
			frames[i] = new OpenGlFrame(this.framesDone - 2, this.frameSize, bytesPerPixel, frameBuffer);
		}

		return this.create(frames);
	}

	public Map<Channel, F> process() {
		Map<Channel, F> channels = null;

		if (framesDone > 1) {
			// Read pbo to memory
			pbo.bind();
			ByteBuffer pboBuffer = pbo.mapReadOnly();

			channels = new HashMap<>();
			channels.put(Channel.BRGA, readFromPbo(pboBuffer, 4));
			if (withDepth) {
				channels.put(Channel.DEPTH, readFromPbo(pboBuffer, 4));
			}

			pbo.unmap();
			pbo.unbind();
		}

		if (framesDone < renderInfo.getTotalFrames()) {
			float partialTicks = renderInfo.updateForNextFrame();
			// Then fill it again
			for (D data : this.data) {
				renderFrame(framesDone, partialTicks, data);
			}
		}

		framesDone++;
		swapPBOs();
		return channels;
	}

	protected OpenGlFrame captureFrame(int frameId, D captureData) {
		this.pbo.bind();
		int offset = captureData.ordinal() * this.getFrameWidth() * this.getFrameHeight() * 4;
		this.frameBuffer().bindWrite(true);
		GL11.glReadPixels(0, 0, this.getFrameWidth(), this.getFrameHeight(), 32993, 5121, (long) offset);
		if (this.withDepth) {
			offset += this.data.length * this.getFrameWidth() * this.getFrameHeight() * 4;
			GL11.glReadPixels(0, 0, this.getFrameWidth(), this.getFrameHeight(), 6402, 5126, (long) offset);
		}

		this.frameBuffer().unbindWrite();
		this.pbo.unbind();
		return null;
	}

	public void close() throws IOException {
		super.close();
		this.pbo.delete();
		this.otherPBO.delete();
	}
}

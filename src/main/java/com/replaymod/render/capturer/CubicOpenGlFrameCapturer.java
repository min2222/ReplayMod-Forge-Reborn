package com.replaymod.render.capturer;

import java.util.Collections;
import java.util.Map;

import com.replaymod.render.frame.CubicOpenGlFrame;
import com.replaymod.render.rendering.Channel;

public class CubicOpenGlFrameCapturer extends OpenGlFrameCapturer<CubicOpenGlFrame, CubicOpenGlFrameCapturer.Data> {
	private final int frameSize;

	public CubicOpenGlFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo, int frameSize) {
		super(worldRenderer, renderInfo);
		this.frameSize = frameSize;
		worldRenderer.setOmnidirectional(true);
	}

	protected int getFrameWidth() {
		return this.frameSize;
	}

	protected int getFrameHeight() {
		return this.frameSize;
	}

	public Map<Channel, CubicOpenGlFrame> process() {
		float partialTicks = this.renderInfo.updateForNextFrame();
		int frameId = this.framesDone++;
		CubicOpenGlFrame frame = new CubicOpenGlFrame(
				this.renderFrame(frameId, partialTicks, CubicOpenGlFrameCapturer.Data.LEFT),
				this.renderFrame(frameId, partialTicks, CubicOpenGlFrameCapturer.Data.RIGHT),
				this.renderFrame(frameId, partialTicks, CubicOpenGlFrameCapturer.Data.FRONT),
				this.renderFrame(frameId, partialTicks, CubicOpenGlFrameCapturer.Data.BACK),
				this.renderFrame(frameId, partialTicks, CubicOpenGlFrameCapturer.Data.TOP),
				this.renderFrame(frameId, partialTicks, CubicOpenGlFrameCapturer.Data.BOTTOM));
		return Collections.singletonMap(Channel.BRGA, frame);
	}

	public static enum Data implements CaptureData {
		LEFT, RIGHT, FRONT, BACK, TOP, BOTTOM;

		// $FF: synthetic method
		private static CubicOpenGlFrameCapturer.Data[] $values() {
			return new CubicOpenGlFrameCapturer.Data[] { LEFT, RIGHT, FRONT, BACK, TOP, BOTTOM };
		}
	}
}

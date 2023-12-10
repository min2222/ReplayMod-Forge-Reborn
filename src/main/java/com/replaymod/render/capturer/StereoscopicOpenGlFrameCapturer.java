package com.replaymod.render.capturer;

import java.util.Collections;
import java.util.Map;

import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.frame.StereoscopicOpenGlFrame;
import com.replaymod.render.rendering.Channel;

public class StereoscopicOpenGlFrameCapturer
		extends OpenGlFrameCapturer<StereoscopicOpenGlFrame, StereoscopicOpenGlFrameCapturer.Data> {
	public StereoscopicOpenGlFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo) {
		super(worldRenderer, renderInfo);
	}

	protected int getFrameWidth() {
		return super.getFrameWidth() / 2;
	}

	public Map<Channel, StereoscopicOpenGlFrame> process() {
		float partialTicks = this.renderInfo.updateForNextFrame();
		int frameId = this.framesDone++;
		OpenGlFrame left = this.renderFrame(frameId, partialTicks, StereoscopicOpenGlFrameCapturer.Data.LEFT_EYE);
		OpenGlFrame right = this.renderFrame(frameId, partialTicks, StereoscopicOpenGlFrameCapturer.Data.RIGHT_EYE);
		StereoscopicOpenGlFrame frame = new StereoscopicOpenGlFrame(left, right);
		return Collections.singletonMap(Channel.BRGA, frame);
	}

	public static enum Data implements CaptureData {
		LEFT_EYE, RIGHT_EYE;

		// $FF: synthetic method
		private static StereoscopicOpenGlFrameCapturer.Data[] $values() {
			return new StereoscopicOpenGlFrameCapturer.Data[] { LEFT_EYE, RIGHT_EYE };
		}
	}
}

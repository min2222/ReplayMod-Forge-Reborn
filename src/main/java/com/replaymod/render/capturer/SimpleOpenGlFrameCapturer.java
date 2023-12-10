package com.replaymod.render.capturer;

import java.util.Collections;
import java.util.Map;

import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.rendering.Channel;

public class SimpleOpenGlFrameCapturer extends OpenGlFrameCapturer<OpenGlFrame, CaptureData> {
	public SimpleOpenGlFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo) {
		super(worldRenderer, renderInfo);
	}

	public Map<Channel, OpenGlFrame> process() {
		float partialTicks = this.renderInfo.updateForNextFrame();
		OpenGlFrame frame = this.renderFrame(this.framesDone++, partialTicks);
		return Collections.singletonMap(Channel.BRGA, frame);
	}
}

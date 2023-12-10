package com.replaymod.render.capturer;

import com.replaymod.render.frame.OpenGlFrame;

public class SimplePboOpenGlFrameCapturer
		extends PboOpenGlFrameCapturer<OpenGlFrame, SimplePboOpenGlFrameCapturer.SinglePass> {
	public SimplePboOpenGlFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo) {
		super(worldRenderer, renderInfo, SimplePboOpenGlFrameCapturer.SinglePass.class,
				renderInfo.getFrameSize().getWidth() * renderInfo.getFrameSize().getHeight());
	}

	protected OpenGlFrame create(OpenGlFrame[] from) {
		return from[0];
	}

	public static enum SinglePass implements CaptureData {
		SINGLE_PASS;

		// $FF: synthetic method
		private static SimplePboOpenGlFrameCapturer.SinglePass[] $values() {
			return new SimplePboOpenGlFrameCapturer.SinglePass[] { SINGLE_PASS };
		}
	}
}

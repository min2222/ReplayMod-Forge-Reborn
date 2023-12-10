package com.replaymod.render.processor;

import java.io.IOException;

import com.replaymod.render.rendering.Frame;
import com.replaymod.render.rendering.FrameProcessor;

public abstract class AbstractFrameProcessor<R extends Frame, P extends Frame> implements FrameProcessor<R, P> {
	public void close() throws IOException {
	}
}

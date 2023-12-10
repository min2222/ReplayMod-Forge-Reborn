package com.replaymod.render.capturer;

import java.io.Closeable;

public interface WorldRenderer extends Closeable {
	void renderWorld(float f, CaptureData captureData);

	void setOmnidirectional(boolean bl);
}

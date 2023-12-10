package com.replaymod.render.processor;

import java.nio.FloatBuffer;

import com.replaymod.render.frame.BitmapFrame;

public class GlToAbsoluteDepthProcessor extends AbstractFrameProcessor<BitmapFrame, BitmapFrame> {
	private final float a;
	private final float b;
	private final float c;

	public GlToAbsoluteDepthProcessor(float near, float far) {
		this.a = 2.0F * near * far;
		this.b = far + near;
		this.c = far - near;
	}

	public BitmapFrame process(BitmapFrame frame) {
		FloatBuffer buffer = frame.getByteBuffer().asFloatBuffer();
		int len = buffer.remaining();

		for (int i = 0; i < len; ++i) {
			float z = buffer.get(i);
			z = this.a / (this.b - this.c * (2.0F * z - 1.0F));
			buffer.put(i, z);
		}

		return frame;
	}
}

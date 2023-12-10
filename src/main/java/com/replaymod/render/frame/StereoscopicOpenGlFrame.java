package com.replaymod.render.frame;

import org.apache.commons.lang3.Validate;

import com.replaymod.render.rendering.Frame;

public class StereoscopicOpenGlFrame implements Frame {
	private final OpenGlFrame left;
	private final OpenGlFrame right;

	public StereoscopicOpenGlFrame(OpenGlFrame left, OpenGlFrame right) {
		Validate.isTrue(left.getFrameId() == right.getFrameId(), "Frame ids do not match.");
		Validate.isTrue(left.getByteBuffer().remaining() == right.getByteBuffer().remaining(),
				"Buffer size does not match.");
		this.left = left;
		this.right = right;
	}

	public int getFrameId() {
		return this.left.getFrameId();
	}

	public OpenGlFrame getLeft() {
		return this.left;
	}

	public OpenGlFrame getRight() {
		return this.right;
	}
}

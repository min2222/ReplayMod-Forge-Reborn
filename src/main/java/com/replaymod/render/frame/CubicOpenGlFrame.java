package com.replaymod.render.frame;

import org.apache.commons.lang3.Validate;

import com.replaymod.render.rendering.Frame;

public class CubicOpenGlFrame implements Frame {
	private final OpenGlFrame left;
	private final OpenGlFrame right;
	private final OpenGlFrame front;
	private final OpenGlFrame back;
	private final OpenGlFrame top;
	private final OpenGlFrame bottom;

	public CubicOpenGlFrame(OpenGlFrame left, OpenGlFrame right, OpenGlFrame front, OpenGlFrame back, OpenGlFrame top,
			OpenGlFrame bottom) {
		Validate.isTrue(left.getFrameId() == right.getFrameId() && right.getFrameId() == front.getFrameId()
				&& front.getFrameId() == back.getFrameId() && back.getFrameId() == top.getFrameId()
				&& top.getFrameId() == bottom.getFrameId(), "Frame ids do not match.");
		Validate.isTrue(
				left.getByteBuffer().remaining() == right.getByteBuffer().remaining()
						&& right.getByteBuffer().remaining() == front.getByteBuffer().remaining()
						&& front.getByteBuffer().remaining() == back.getByteBuffer().remaining()
						&& back.getByteBuffer().remaining() == top.getByteBuffer().remaining()
						&& top.getByteBuffer().remaining() == bottom.getByteBuffer().remaining(),
				"Buffer size does not match.");
		this.left = left;
		this.right = right;
		this.front = front;
		this.back = back;
		this.top = top;
		this.bottom = bottom;
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

	public OpenGlFrame getFront() {
		return this.front;
	}

	public OpenGlFrame getBack() {
		return this.back;
	}

	public OpenGlFrame getTop() {
		return this.top;
	}

	public OpenGlFrame getBottom() {
		return this.bottom;
	}
}

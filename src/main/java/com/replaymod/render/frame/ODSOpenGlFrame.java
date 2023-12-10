package com.replaymod.render.frame;

import org.apache.commons.lang3.Validate;

import com.replaymod.render.rendering.Frame;

public class ODSOpenGlFrame implements Frame {
	private final CubicOpenGlFrame left;
	private final CubicOpenGlFrame right;

	public ODSOpenGlFrame(CubicOpenGlFrame left, CubicOpenGlFrame right) {
		Validate.isTrue(left.getFrameId() == right.getFrameId(), "Frame ids do not match.");
		this.left = left;
		this.right = right;
	}

	public int getFrameId() {
		return this.left.getFrameId();
	}

	public CubicOpenGlFrame getLeft() {
		return this.left;
	}

	public CubicOpenGlFrame getRight() {
		return this.right;
	}
}

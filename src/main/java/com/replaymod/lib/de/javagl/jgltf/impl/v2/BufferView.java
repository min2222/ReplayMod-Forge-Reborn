package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class BufferView extends GlTFChildOfRootProperty {
	private Integer buffer;
	private Integer byteOffset;
	private Integer byteLength;
	private Integer byteStride;
	private Integer target;

	public void setBuffer(Integer buffer) {
		if (buffer == null) {
			throw new NullPointerException("Invalid value for buffer: " + buffer + ", may not be null");
		} else {
			this.buffer = buffer;
		}
	}

	public Integer getBuffer() {
		return this.buffer;
	}

	public void setByteOffset(Integer byteOffset) {
		if (byteOffset == null) {
			this.byteOffset = byteOffset;
		} else if (byteOffset < 0) {
			throw new IllegalArgumentException("byteOffset < 0");
		} else {
			this.byteOffset = byteOffset;
		}
	}

	public Integer getByteOffset() {
		return this.byteOffset;
	}

	public Integer defaultByteOffset() {
		return 0;
	}

	public void setByteLength(Integer byteLength) {
		if (byteLength == null) {
			throw new NullPointerException("Invalid value for byteLength: " + byteLength + ", may not be null");
		} else if (byteLength < 1) {
			throw new IllegalArgumentException("byteLength < 1");
		} else {
			this.byteLength = byteLength;
		}
	}

	public Integer getByteLength() {
		return this.byteLength;
	}

	public void setByteStride(Integer byteStride) {
		if (byteStride == null) {
			this.byteStride = byteStride;
		} else if (byteStride > 252) {
			throw new IllegalArgumentException("byteStride > 252");
		} else if (byteStride < 4) {
			throw new IllegalArgumentException("byteStride < 4");
		} else {
			this.byteStride = byteStride;
		}
	}

	public Integer getByteStride() {
		return this.byteStride;
	}

	public void setTarget(Integer target) {
		if (target == null) {
			this.target = target;
		} else if (target != 34962 && target != 34963) {
			throw new IllegalArgumentException("Invalid value for target: " + target + ", valid: [34962, 34963]");
		} else {
			this.target = target;
		}
	}

	public Integer getTarget() {
		return this.target;
	}
}

package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class AccessorSparseValues extends GlTFProperty {
	private Integer bufferView;
	private Integer byteOffset;

	public void setBufferView(Integer bufferView) {
		if (bufferView == null) {
			throw new NullPointerException("Invalid value for bufferView: " + bufferView + ", may not be null");
		} else {
			this.bufferView = bufferView;
		}
	}

	public Integer getBufferView() {
		return this.bufferView;
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
}

package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class Buffer extends GlTFChildOfRootProperty {
	private String uri;
	private Integer byteLength;

	public void setUri(String uri) {
		if (uri == null) {
			this.uri = uri;
		} else {
			this.uri = uri;
		}
	}

	public String getUri() {
		return this.uri;
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
}

package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class Image extends GlTFChildOfRootProperty {
	private String uri;
	private String mimeType;
	private Integer bufferView;

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

	public void setMimeType(String mimeType) {
		if (mimeType == null) {
			this.mimeType = mimeType;
		} else if (!"image/jpeg".equals(mimeType) && !"image/png".equals(mimeType)) {
			throw new IllegalArgumentException(
					"Invalid value for mimeType: " + mimeType + ", valid: [\"image/jpeg\", \"image/png\"]");
		} else {
			this.mimeType = mimeType;
		}
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public void setBufferView(Integer bufferView) {
		if (bufferView == null) {
			this.bufferView = bufferView;
		} else {
			this.bufferView = bufferView;
		}
	}

	public Integer getBufferView() {
		return this.bufferView;
	}
}

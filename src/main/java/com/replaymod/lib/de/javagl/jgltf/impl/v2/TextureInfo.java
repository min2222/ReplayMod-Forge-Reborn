package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class TextureInfo extends GlTFProperty {
	private Integer index;
	private Integer texCoord;

	public void setIndex(Integer index) {
		if (index == null) {
			throw new NullPointerException("Invalid value for index: " + index + ", may not be null");
		} else {
			this.index = index;
		}
	}

	public Integer getIndex() {
		return this.index;
	}

	public void setTexCoord(Integer texCoord) {
		if (texCoord == null) {
			this.texCoord = texCoord;
		} else if (texCoord < 0) {
			throw new IllegalArgumentException("texCoord < 0");
		} else {
			this.texCoord = texCoord;
		}
	}

	public Integer getTexCoord() {
		return this.texCoord;
	}

	public Integer defaultTexCoord() {
		return 0;
	}
}

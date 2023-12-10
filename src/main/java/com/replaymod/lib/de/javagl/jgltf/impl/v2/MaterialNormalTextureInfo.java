package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class MaterialNormalTextureInfo extends TextureInfo {
	private Float scale;

	public void setScale(Float scale) {
		if (scale == null) {
			this.scale = scale;
		} else {
			this.scale = scale;
		}
	}

	public Float getScale() {
		return this.scale;
	}

	public Float defaultScale() {
		return 1.0F;
	}
}

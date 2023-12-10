package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class Texture extends GlTFChildOfRootProperty {
	private Integer sampler;
	private Integer source;

	public void setSampler(Integer sampler) {
		if (sampler == null) {
			this.sampler = sampler;
		} else {
			this.sampler = sampler;
		}
	}

	public Integer getSampler() {
		return this.sampler;
	}

	public void setSource(Integer source) {
		if (source == null) {
			this.source = source;
		} else {
			this.source = source;
		}
	}

	public Integer getSource() {
		return this.source;
	}
}

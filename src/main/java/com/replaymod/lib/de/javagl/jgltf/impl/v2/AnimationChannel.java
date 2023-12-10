package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class AnimationChannel extends GlTFProperty {
	private Integer sampler;
	private AnimationChannelTarget target;

	public void setSampler(Integer sampler) {
		if (sampler == null) {
			throw new NullPointerException("Invalid value for sampler: " + sampler + ", may not be null");
		} else {
			this.sampler = sampler;
		}
	}

	public Integer getSampler() {
		return this.sampler;
	}

	public void setTarget(AnimationChannelTarget target) {
		if (target == null) {
			throw new NullPointerException("Invalid value for target: " + target + ", may not be null");
		} else {
			this.target = target;
		}
	}

	public AnimationChannelTarget getTarget() {
		return this.target;
	}
}

package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class MaterialOcclusionTextureInfo extends TextureInfo {
	private Float strength;

	public void setStrength(Float strength) {
		if (strength == null) {
			this.strength = strength;
		} else if ((double) strength > 1.0D) {
			throw new IllegalArgumentException("strength > 1.0");
		} else if ((double) strength < 0.0D) {
			throw new IllegalArgumentException("strength < 0.0");
		} else {
			this.strength = strength;
		}
	}

	public Float getStrength() {
		return this.strength;
	}

	public Float defaultStrength() {
		return 1.0F;
	}
}

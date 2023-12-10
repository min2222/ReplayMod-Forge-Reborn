package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class MaterialPbrMetallicRoughness extends GlTFProperty {
	private float[] baseColorFactor;
	private TextureInfo baseColorTexture;
	private Float metallicFactor;
	private Float roughnessFactor;
	private TextureInfo metallicRoughnessTexture;

	public void setBaseColorFactor(float[] baseColorFactor) {
		if (baseColorFactor == null) {
			this.baseColorFactor = baseColorFactor;
		} else if (baseColorFactor.length < 4) {
			throw new IllegalArgumentException("Number of baseColorFactor elements is < 4");
		} else if (baseColorFactor.length > 4) {
			throw new IllegalArgumentException("Number of baseColorFactor elements is > 4");
		} else {
			float[] var2 = baseColorFactor;
			int var3 = baseColorFactor.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				float baseColorFactorElement = var2[var4];
				if ((double) baseColorFactorElement > 1.0D) {
					throw new IllegalArgumentException("baseColorFactorElement > 1.0");
				}

				if ((double) baseColorFactorElement < 0.0D) {
					throw new IllegalArgumentException("baseColorFactorElement < 0.0");
				}
			}

			this.baseColorFactor = baseColorFactor;
		}
	}

	public float[] getBaseColorFactor() {
		return this.baseColorFactor;
	}

	public float[] defaultBaseColorFactor() {
		return new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
	}

	public void setBaseColorTexture(TextureInfo baseColorTexture) {
		if (baseColorTexture == null) {
			this.baseColorTexture = baseColorTexture;
		} else {
			this.baseColorTexture = baseColorTexture;
		}
	}

	public TextureInfo getBaseColorTexture() {
		return this.baseColorTexture;
	}

	public void setMetallicFactor(Float metallicFactor) {
		if (metallicFactor == null) {
			this.metallicFactor = metallicFactor;
		} else if ((double) metallicFactor > 1.0D) {
			throw new IllegalArgumentException("metallicFactor > 1.0");
		} else if ((double) metallicFactor < 0.0D) {
			throw new IllegalArgumentException("metallicFactor < 0.0");
		} else {
			this.metallicFactor = metallicFactor;
		}
	}

	public Float getMetallicFactor() {
		return this.metallicFactor;
	}

	public Float defaultMetallicFactor() {
		return 1.0F;
	}

	public void setRoughnessFactor(Float roughnessFactor) {
		if (roughnessFactor == null) {
			this.roughnessFactor = roughnessFactor;
		} else if ((double) roughnessFactor > 1.0D) {
			throw new IllegalArgumentException("roughnessFactor > 1.0");
		} else if ((double) roughnessFactor < 0.0D) {
			throw new IllegalArgumentException("roughnessFactor < 0.0");
		} else {
			this.roughnessFactor = roughnessFactor;
		}
	}

	public Float getRoughnessFactor() {
		return this.roughnessFactor;
	}

	public Float defaultRoughnessFactor() {
		return 1.0F;
	}

	public void setMetallicRoughnessTexture(TextureInfo metallicRoughnessTexture) {
		if (metallicRoughnessTexture == null) {
			this.metallicRoughnessTexture = metallicRoughnessTexture;
		} else {
			this.metallicRoughnessTexture = metallicRoughnessTexture;
		}
	}

	public TextureInfo getMetallicRoughnessTexture() {
		return this.metallicRoughnessTexture;
	}
}

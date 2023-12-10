package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class Material extends GlTFChildOfRootProperty {
	private MaterialPbrMetallicRoughness pbrMetallicRoughness;
	private MaterialNormalTextureInfo normalTexture;
	private MaterialOcclusionTextureInfo occlusionTexture;
	private TextureInfo emissiveTexture;
	private float[] emissiveFactor;
	private String alphaMode;
	private Float alphaCutoff;
	private Boolean doubleSided;

	public void setPbrMetallicRoughness(MaterialPbrMetallicRoughness pbrMetallicRoughness) {
		if (pbrMetallicRoughness == null) {
			this.pbrMetallicRoughness = pbrMetallicRoughness;
		} else {
			this.pbrMetallicRoughness = pbrMetallicRoughness;
		}
	}

	public MaterialPbrMetallicRoughness getPbrMetallicRoughness() {
		return this.pbrMetallicRoughness;
	}

	public void setNormalTexture(MaterialNormalTextureInfo normalTexture) {
		if (normalTexture == null) {
			this.normalTexture = normalTexture;
		} else {
			this.normalTexture = normalTexture;
		}
	}

	public MaterialNormalTextureInfo getNormalTexture() {
		return this.normalTexture;
	}

	public void setOcclusionTexture(MaterialOcclusionTextureInfo occlusionTexture) {
		if (occlusionTexture == null) {
			this.occlusionTexture = occlusionTexture;
		} else {
			this.occlusionTexture = occlusionTexture;
		}
	}

	public MaterialOcclusionTextureInfo getOcclusionTexture() {
		return this.occlusionTexture;
	}

	public void setEmissiveTexture(TextureInfo emissiveTexture) {
		if (emissiveTexture == null) {
			this.emissiveTexture = emissiveTexture;
		} else {
			this.emissiveTexture = emissiveTexture;
		}
	}

	public TextureInfo getEmissiveTexture() {
		return this.emissiveTexture;
	}

	public void setEmissiveFactor(float[] emissiveFactor) {
		if (emissiveFactor == null) {
			this.emissiveFactor = emissiveFactor;
		} else if (emissiveFactor.length < 3) {
			throw new IllegalArgumentException("Number of emissiveFactor elements is < 3");
		} else if (emissiveFactor.length > 3) {
			throw new IllegalArgumentException("Number of emissiveFactor elements is > 3");
		} else {
			float[] var2 = emissiveFactor;
			int var3 = emissiveFactor.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				float emissiveFactorElement = var2[var4];
				if ((double) emissiveFactorElement > 1.0D) {
					throw new IllegalArgumentException("emissiveFactorElement > 1.0");
				}

				if ((double) emissiveFactorElement < 0.0D) {
					throw new IllegalArgumentException("emissiveFactorElement < 0.0");
				}
			}

			this.emissiveFactor = emissiveFactor;
		}
	}

	public float[] getEmissiveFactor() {
		return this.emissiveFactor;
	}

	public float[] defaultEmissiveFactor() {
		return new float[] { 0.0F, 0.0F, 0.0F };
	}

	public void setAlphaMode(String alphaMode) {
		if (alphaMode == null) {
			this.alphaMode = alphaMode;
		} else if (!"OPAQUE".equals(alphaMode) && !"MASK".equals(alphaMode) && !"BLEND".equals(alphaMode)) {
			throw new IllegalArgumentException(
					"Invalid value for alphaMode: " + alphaMode + ", valid: [\"OPAQUE\", \"MASK\", \"BLEND\"]");
		} else {
			this.alphaMode = alphaMode;
		}
	}

	public String getAlphaMode() {
		return this.alphaMode;
	}

	public String defaultAlphaMode() {
		return "OPAQUE";
	}

	public void setAlphaCutoff(Float alphaCutoff) {
		if (alphaCutoff == null) {
			this.alphaCutoff = alphaCutoff;
		} else if ((double) alphaCutoff < 0.0D) {
			throw new IllegalArgumentException("alphaCutoff < 0.0");
		} else {
			this.alphaCutoff = alphaCutoff;
		}
	}

	public Float getAlphaCutoff() {
		return this.alphaCutoff;
	}

	public Float defaultAlphaCutoff() {
		return 0.5F;
	}

	public void setDoubleSided(Boolean doubleSided) {
		if (doubleSided == null) {
			this.doubleSided = doubleSided;
		} else {
			this.doubleSided = doubleSided;
		}
	}

	public Boolean isDoubleSided() {
		return this.doubleSided;
	}

	public Boolean defaultDoubleSided() {
		return false;
	}
}

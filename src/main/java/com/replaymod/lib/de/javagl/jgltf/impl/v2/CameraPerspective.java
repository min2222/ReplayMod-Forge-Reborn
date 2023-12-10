package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class CameraPerspective extends GlTFProperty {
	private Float aspectRatio;
	private Float yfov;
	private Float zfar;
	private Float znear;

	public void setAspectRatio(Float aspectRatio) {
		if (aspectRatio == null) {
			this.aspectRatio = aspectRatio;
		} else if ((double) aspectRatio <= 0.0D) {
			throw new IllegalArgumentException("aspectRatio <= 0.0");
		} else {
			this.aspectRatio = aspectRatio;
		}
	}

	public Float getAspectRatio() {
		return this.aspectRatio;
	}

	public void setYfov(Float yfov) {
		if (yfov == null) {
			throw new NullPointerException("Invalid value for yfov: " + yfov + ", may not be null");
		} else if ((double) yfov <= 0.0D) {
			throw new IllegalArgumentException("yfov <= 0.0");
		} else {
			this.yfov = yfov;
		}
	}

	public Float getYfov() {
		return this.yfov;
	}

	public void setZfar(Float zfar) {
		if (zfar == null) {
			this.zfar = zfar;
		} else if ((double) zfar <= 0.0D) {
			throw new IllegalArgumentException("zfar <= 0.0");
		} else {
			this.zfar = zfar;
		}
	}

	public Float getZfar() {
		return this.zfar;
	}

	public void setZnear(Float znear) {
		if (znear == null) {
			throw new NullPointerException("Invalid value for znear: " + znear + ", may not be null");
		} else if ((double) znear <= 0.0D) {
			throw new IllegalArgumentException("znear <= 0.0");
		} else {
			this.znear = znear;
		}
	}

	public Float getZnear() {
		return this.znear;
	}
}

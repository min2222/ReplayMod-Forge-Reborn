package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class CameraOrthographic extends GlTFProperty {
	private Float xmag;
	private Float ymag;
	private Float zfar;
	private Float znear;

	public void setXmag(Float xmag) {
		if (xmag == null) {
			throw new NullPointerException("Invalid value for xmag: " + xmag + ", may not be null");
		} else {
			this.xmag = xmag;
		}
	}

	public Float getXmag() {
		return this.xmag;
	}

	public void setYmag(Float ymag) {
		if (ymag == null) {
			throw new NullPointerException("Invalid value for ymag: " + ymag + ", may not be null");
		} else {
			this.ymag = ymag;
		}
	}

	public Float getYmag() {
		return this.ymag;
	}

	public void setZfar(Float zfar) {
		if (zfar == null) {
			throw new NullPointerException("Invalid value for zfar: " + zfar + ", may not be null");
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
		} else if ((double) znear < 0.0D) {
			throw new IllegalArgumentException("znear < 0.0");
		} else {
			this.znear = znear;
		}
	}

	public Float getZnear() {
		return this.znear;
	}
}

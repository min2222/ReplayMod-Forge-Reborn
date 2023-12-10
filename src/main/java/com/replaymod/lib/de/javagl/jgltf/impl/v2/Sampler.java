package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class Sampler extends GlTFChildOfRootProperty {
	private Integer magFilter;
	private Integer minFilter;
	private Integer wrapS;
	private Integer wrapT;

	public void setMagFilter(Integer magFilter) {
		if (magFilter == null) {
			this.magFilter = magFilter;
		} else if (magFilter != 9728 && magFilter != 9729) {
			throw new IllegalArgumentException("Invalid value for magFilter: " + magFilter + ", valid: [9728, 9729]");
		} else {
			this.magFilter = magFilter;
		}
	}

	public Integer getMagFilter() {
		return this.magFilter;
	}

	public void setMinFilter(Integer minFilter) {
		if (minFilter == null) {
			this.minFilter = minFilter;
		} else if (minFilter != 9728 && minFilter != 9729 && minFilter != 9984 && minFilter != 9985 && minFilter != 9986
				&& minFilter != 9987) {
			throw new IllegalArgumentException(
					"Invalid value for minFilter: " + minFilter + ", valid: [9728, 9729, 9984, 9985, 9986, 9987]");
		} else {
			this.minFilter = minFilter;
		}
	}

	public Integer getMinFilter() {
		return this.minFilter;
	}

	public void setWrapS(Integer wrapS) {
		if (wrapS == null) {
			this.wrapS = wrapS;
		} else if (wrapS != 33071 && wrapS != 33648 && wrapS != 10497) {
			throw new IllegalArgumentException("Invalid value for wrapS: " + wrapS + ", valid: [33071, 33648, 10497]");
		} else {
			this.wrapS = wrapS;
		}
	}

	public Integer getWrapS() {
		return this.wrapS;
	}

	public Integer defaultWrapS() {
		return 10497;
	}

	public void setWrapT(Integer wrapT) {
		if (wrapT == null) {
			this.wrapT = wrapT;
		} else if (wrapT != 33071 && wrapT != 33648 && wrapT != 10497) {
			throw new IllegalArgumentException("Invalid value for wrapT: " + wrapT + ", valid: [33071, 33648, 10497]");
		} else {
			this.wrapT = wrapT;
		}
	}

	public Integer getWrapT() {
		return this.wrapT;
	}

	public Integer defaultWrapT() {
		return 10497;
	}
}

package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class AnimationChannelTarget extends GlTFProperty {
	private Integer node;
	private String path;

	public void setNode(Integer node) {
		if (node == null) {
			this.node = node;
		} else {
			this.node = node;
		}
	}

	public Integer getNode() {
		return this.node;
	}

	public void setPath(String path) {
		if (path == null) {
			throw new NullPointerException("Invalid value for path: " + path + ", may not be null");
		} else if (!"translation".equals(path) && !"rotation".equals(path) && !"scale".equals(path)
				&& !"weights".equals(path)) {
			throw new IllegalArgumentException("Invalid value for path: " + path
					+ ", valid: [\"translation\", \"rotation\", \"scale\", \"weights\"]");
		} else {
			this.path = path;
		}
	}

	public String getPath() {
		return this.path;
	}
}

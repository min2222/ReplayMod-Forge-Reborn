package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class Camera extends GlTFChildOfRootProperty {
	private CameraOrthographic orthographic;
	private CameraPerspective perspective;
	private String type;

	public void setOrthographic(CameraOrthographic orthographic) {
		if (orthographic == null) {
			this.orthographic = orthographic;
		} else {
			this.orthographic = orthographic;
		}
	}

	public CameraOrthographic getOrthographic() {
		return this.orthographic;
	}

	public void setPerspective(CameraPerspective perspective) {
		if (perspective == null) {
			this.perspective = perspective;
		} else {
			this.perspective = perspective;
		}
	}

	public CameraPerspective getPerspective() {
		return this.perspective;
	}

	public void setType(String type) {
		if (type == null) {
			throw new NullPointerException("Invalid value for type: " + type + ", may not be null");
		} else if (!"perspective".equals(type) && !"orthographic".equals(type)) {
			throw new IllegalArgumentException(
					"Invalid value for type: " + type + ", valid: [\"perspective\", \"orthographic\"]");
		} else {
			this.type = type;
		}
	}

	public String getType() {
		return this.type;
	}
}

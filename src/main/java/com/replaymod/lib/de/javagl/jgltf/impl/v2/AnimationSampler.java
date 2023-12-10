package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class AnimationSampler extends GlTFProperty {
	private Integer input;
	private String interpolation;
	private Integer output;

	public void setInput(Integer input) {
		if (input == null) {
			throw new NullPointerException("Invalid value for input: " + input + ", may not be null");
		} else {
			this.input = input;
		}
	}

	public Integer getInput() {
		return this.input;
	}

	public void setInterpolation(String interpolation) {
		if (interpolation == null) {
			this.interpolation = interpolation;
		} else if (!"LINEAR".equals(interpolation) && !"STEP".equals(interpolation)
				&& !"CATMULLROMSPLINE".equals(interpolation) && !"CUBICSPLINE".equals(interpolation)) {
			throw new IllegalArgumentException("Invalid value for interpolation: " + interpolation
					+ ", valid: [\"LINEAR\", \"STEP\", \"CATMULLROMSPLINE\", \"CUBICSPLINE\"]");
		} else {
			this.interpolation = interpolation;
		}
	}

	public String getInterpolation() {
		return this.interpolation;
	}

	public String defaultInterpolation() {
		return "LINEAR";
	}

	public void setOutput(Integer output) {
		if (output == null) {
			throw new NullPointerException("Invalid value for output: " + output + ", may not be null");
		} else {
			this.output = output;
		}
	}

	public Integer getOutput() {
		return this.output;
	}
}

package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class Accessor extends GlTFChildOfRootProperty {
	private Integer bufferView;
	private Integer byteOffset;
	private Integer componentType;
	private Boolean normalized;
	private Integer count;
	private String type;
	private Number[] max;
	private Number[] min;
	private AccessorSparse sparse;

	public void setBufferView(Integer bufferView) {
		if (bufferView == null) {
			this.bufferView = bufferView;
		} else {
			this.bufferView = bufferView;
		}
	}

	public Integer getBufferView() {
		return this.bufferView;
	}

	public void setByteOffset(Integer byteOffset) {
		if (byteOffset == null) {
			this.byteOffset = byteOffset;
		} else if (byteOffset < 0) {
			throw new IllegalArgumentException("byteOffset < 0");
		} else {
			this.byteOffset = byteOffset;
		}
	}

	public Integer getByteOffset() {
		return this.byteOffset;
	}

	public Integer defaultByteOffset() {
		return 0;
	}

	public void setComponentType(Integer componentType) {
		if (componentType == null) {
			throw new NullPointerException("Invalid value for componentType: " + componentType + ", may not be null");
		} else if (componentType != 5120 && componentType != 5121 && componentType != 5122 && componentType != 5123
				&& componentType != 5125 && componentType != 5126) {
			throw new IllegalArgumentException("Invalid value for componentType: " + componentType
					+ ", valid: [5120, 5121, 5122, 5123, 5125, 5126]");
		} else {
			this.componentType = componentType;
		}
	}

	public Integer getComponentType() {
		return this.componentType;
	}

	public void setNormalized(Boolean normalized) {
		if (normalized == null) {
			this.normalized = normalized;
		} else {
			this.normalized = normalized;
		}
	}

	public Boolean isNormalized() {
		return this.normalized;
	}

	public Boolean defaultNormalized() {
		return false;
	}

	public void setCount(Integer count) {
		if (count == null) {
			throw new NullPointerException("Invalid value for count: " + count + ", may not be null");
		} else if (count < 1) {
			throw new IllegalArgumentException("count < 1");
		} else {
			this.count = count;
		}
	}

	public Integer getCount() {
		return this.count;
	}

	public void setType(String type) {
		if (type == null) {
			throw new NullPointerException("Invalid value for type: " + type + ", may not be null");
		} else if (!"SCALAR".equals(type) && !"VEC2".equals(type) && !"VEC3".equals(type) && !"VEC4".equals(type)
				&& !"MAT2".equals(type) && !"MAT3".equals(type) && !"MAT4".equals(type)) {
			throw new IllegalArgumentException("Invalid value for type: " + type
					+ ", valid: [\"SCALAR\", \"VEC2\", \"VEC3\", \"VEC4\", \"MAT2\", \"MAT3\", \"MAT4\"]");
		} else {
			this.type = type;
		}
	}

	public String getType() {
		return this.type;
	}

	public void setMax(Number[] max) {
		if (max == null) {
			this.max = max;
		} else if (max.length < 1) {
			throw new IllegalArgumentException("Number of max elements is < 1");
		} else if (max.length > 16) {
			throw new IllegalArgumentException("Number of max elements is > 16");
		} else {
			this.max = max;
		}
	}

	public Number[] getMax() {
		return this.max;
	}

	public void setMin(Number[] min) {
		if (min == null) {
			this.min = min;
		} else if (min.length < 1) {
			throw new IllegalArgumentException("Number of min elements is < 1");
		} else if (min.length > 16) {
			throw new IllegalArgumentException("Number of min elements is > 16");
		} else {
			this.min = min;
		}
	}

	public Number[] getMin() {
		return this.min;
	}

	public void setSparse(AccessorSparse sparse) {
		if (sparse == null) {
			this.sparse = sparse;
		} else {
			this.sparse = sparse;
		}
	}

	public AccessorSparse getSparse() {
		return this.sparse;
	}
}

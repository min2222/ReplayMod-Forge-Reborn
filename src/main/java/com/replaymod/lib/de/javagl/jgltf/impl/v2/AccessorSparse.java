package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class AccessorSparse extends GlTFProperty {
	private Integer count;
	private AccessorSparseIndices indices;
	private AccessorSparseValues values;

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

	public void setIndices(AccessorSparseIndices indices) {
		if (indices == null) {
			throw new NullPointerException("Invalid value for indices: " + indices + ", may not be null");
		} else {
			this.indices = indices;
		}
	}

	public AccessorSparseIndices getIndices() {
		return this.indices;
	}

	public void setValues(AccessorSparseValues values) {
		if (values == null) {
			throw new NullPointerException("Invalid value for values: " + values + ", may not be null");
		} else {
			this.values = values;
		}
	}

	public AccessorSparseValues getValues() {
		return this.values;
	}
}

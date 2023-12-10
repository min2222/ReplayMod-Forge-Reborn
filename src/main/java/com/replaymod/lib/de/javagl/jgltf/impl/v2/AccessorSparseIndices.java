package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class AccessorSparseIndices extends GlTFProperty {
	private Integer bufferView;
	private Integer byteOffset;
	private Integer componentType;

	public void setBufferView(Integer bufferView) {
		if (bufferView == null) {
			throw new NullPointerException("Invalid value for bufferView: " + bufferView + ", may not be null");
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
		} else if (componentType != 5121 && componentType != 5123 && componentType != 5125) {
			throw new IllegalArgumentException(
					"Invalid value for componentType: " + componentType + ", valid: [5121, 5123, 5125]");
		} else {
			this.componentType = componentType;
		}
	}

	public Integer getComponentType() {
		return this.componentType;
	}
}

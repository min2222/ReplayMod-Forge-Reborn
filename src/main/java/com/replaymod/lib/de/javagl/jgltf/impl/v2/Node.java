package com.replaymod.lib.de.javagl.jgltf.impl.v2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Node extends GlTFChildOfRootProperty {
	private Integer camera;
	private List<Integer> children;
	private Integer skin;
	private float[] matrix;
	private Integer mesh;
	private float[] rotation;
	private float[] scale;
	private float[] translation;
	private List<Float> weights;

	public void setCamera(Integer camera) {
		if (camera == null) {
			this.camera = camera;
		} else {
			this.camera = camera;
		}
	}

	public Integer getCamera() {
		return this.camera;
	}

	public void setChildren(List<Integer> children) {
		if (children == null) {
			this.children = children;
		} else if (children.size() < 1) {
			throw new IllegalArgumentException("Number of children elements is < 1");
		} else {
			Iterator var2 = children.iterator();

			Integer childrenElement;
			do {
				if (!var2.hasNext()) {
					this.children = children;
					return;
				}

				childrenElement = (Integer) var2.next();
			} while (childrenElement >= 0);

			throw new IllegalArgumentException("childrenElement < 0");
		}
	}

	public List<Integer> getChildren() {
		return this.children;
	}

	public void addChildren(Integer element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Integer> oldList = this.children;
			List<Integer> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.children = newList;
		}
	}

	public void removeChildren(Integer element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Integer> oldList = this.children;
			List<Integer> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.children = null;
			} else {
				this.children = newList;
			}

		}
	}

	public void setSkin(Integer skin) {
		if (skin == null) {
			this.skin = skin;
		} else {
			this.skin = skin;
		}
	}

	public Integer getSkin() {
		return this.skin;
	}

	public void setMatrix(float[] matrix) {
		if (matrix == null) {
			this.matrix = matrix;
		} else if (matrix.length < 16) {
			throw new IllegalArgumentException("Number of matrix elements is < 16");
		} else if (matrix.length > 16) {
			throw new IllegalArgumentException("Number of matrix elements is > 16");
		} else {
			this.matrix = matrix;
		}
	}

	public float[] getMatrix() {
		return this.matrix;
	}

	public float[] defaultMatrix() {
		return new float[] { 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F,
				1.0F };
	}

	public void setMesh(Integer mesh) {
		if (mesh == null) {
			this.mesh = mesh;
		} else {
			this.mesh = mesh;
		}
	}

	public Integer getMesh() {
		return this.mesh;
	}

	public void setRotation(float[] rotation) {
		if (rotation == null) {
			this.rotation = rotation;
		} else if (rotation.length < 4) {
			throw new IllegalArgumentException("Number of rotation elements is < 4");
		} else if (rotation.length > 4) {
			throw new IllegalArgumentException("Number of rotation elements is > 4");
		} else {
			float[] var2 = rotation;
			int var3 = rotation.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				float rotationElement = var2[var4];
				if ((double) rotationElement > 1.0D) {
					throw new IllegalArgumentException("rotationElement > 1.0");
				}

				if ((double) rotationElement < -1.0D) {
					throw new IllegalArgumentException("rotationElement < -1.0");
				}
			}

			this.rotation = rotation;
		}
	}

	public float[] getRotation() {
		return this.rotation;
	}

	public float[] defaultRotation() {
		return new float[] { 0.0F, 0.0F, 0.0F, 1.0F };
	}

	public void setScale(float[] scale) {
		if (scale == null) {
			this.scale = scale;
		} else if (scale.length < 3) {
			throw new IllegalArgumentException("Number of scale elements is < 3");
		} else if (scale.length > 3) {
			throw new IllegalArgumentException("Number of scale elements is > 3");
		} else {
			this.scale = scale;
		}
	}

	public float[] getScale() {
		return this.scale;
	}

	public float[] defaultScale() {
		return new float[] { 1.0F, 1.0F, 1.0F };
	}

	public void setTranslation(float[] translation) {
		if (translation == null) {
			this.translation = translation;
		} else if (translation.length < 3) {
			throw new IllegalArgumentException("Number of translation elements is < 3");
		} else if (translation.length > 3) {
			throw new IllegalArgumentException("Number of translation elements is > 3");
		} else {
			this.translation = translation;
		}
	}

	public float[] getTranslation() {
		return this.translation;
	}

	public float[] defaultTranslation() {
		return new float[] { 0.0F, 0.0F, 0.0F };
	}

	public void setWeights(List<Float> weights) {
		if (weights == null) {
			this.weights = weights;
		} else if (weights.size() < 1) {
			throw new IllegalArgumentException("Number of weights elements is < 1");
		} else {
			this.weights = weights;
		}
	}

	public List<Float> getWeights() {
		return this.weights;
	}

	public void addWeights(Float element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Float> oldList = this.weights;
			List<Float> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.weights = newList;
		}
	}

	public void removeWeights(Float element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Float> oldList = this.weights;
			List<Float> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.weights = null;
			} else {
				this.weights = newList;
			}

		}
	}
}

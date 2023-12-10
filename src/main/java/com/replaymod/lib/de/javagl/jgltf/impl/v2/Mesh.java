package com.replaymod.lib.de.javagl.jgltf.impl.v2;

import java.util.ArrayList;
import java.util.List;

public class Mesh extends GlTFChildOfRootProperty {
	private List<MeshPrimitive> primitives;
	private List<Float> weights;

	public void setPrimitives(List<MeshPrimitive> primitives) {
		if (primitives == null) {
			throw new NullPointerException("Invalid value for primitives: " + primitives + ", may not be null");
		} else if (primitives.size() < 1) {
			throw new IllegalArgumentException("Number of primitives elements is < 1");
		} else {
			this.primitives = primitives;
		}
	}

	public List<MeshPrimitive> getPrimitives() {
		return this.primitives;
	}

	public void addPrimitives(MeshPrimitive element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<MeshPrimitive> oldList = this.primitives;
			List<MeshPrimitive> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.primitives = newList;
		}
	}

	public void removePrimitives(MeshPrimitive element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<MeshPrimitive> oldList = this.primitives;
			List<MeshPrimitive> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			this.primitives = newList;
		}
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

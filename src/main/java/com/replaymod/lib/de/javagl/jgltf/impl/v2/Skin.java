package com.replaymod.lib.de.javagl.jgltf.impl.v2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Skin extends GlTFChildOfRootProperty {
	private Integer inverseBindMatrices;
	private Integer skeleton;
	private List<Integer> joints;

	public void setInverseBindMatrices(Integer inverseBindMatrices) {
		if (inverseBindMatrices == null) {
			this.inverseBindMatrices = inverseBindMatrices;
		} else {
			this.inverseBindMatrices = inverseBindMatrices;
		}
	}

	public Integer getInverseBindMatrices() {
		return this.inverseBindMatrices;
	}

	public void setSkeleton(Integer skeleton) {
		if (skeleton == null) {
			this.skeleton = skeleton;
		} else {
			this.skeleton = skeleton;
		}
	}

	public Integer getSkeleton() {
		return this.skeleton;
	}

	public void setJoints(List<Integer> joints) {
		if (joints == null) {
			throw new NullPointerException("Invalid value for joints: " + joints + ", may not be null");
		} else if (joints.size() < 1) {
			throw new IllegalArgumentException("Number of joints elements is < 1");
		} else {
			Iterator var2 = joints.iterator();

			Integer jointsElement;
			do {
				if (!var2.hasNext()) {
					this.joints = joints;
					return;
				}

				jointsElement = (Integer) var2.next();
			} while (jointsElement >= 0);

			throw new IllegalArgumentException("jointsElement < 0");
		}
	}

	public List<Integer> getJoints() {
		return this.joints;
	}

	public void addJoints(Integer element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Integer> oldList = this.joints;
			List<Integer> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.joints = newList;
		}
	}

	public void removeJoints(Integer element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Integer> oldList = this.joints;
			List<Integer> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			this.joints = newList;
		}
	}
}

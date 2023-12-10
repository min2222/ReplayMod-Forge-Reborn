package com.replaymod.lib.de.javagl.jgltf.impl.v2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Scene extends GlTFChildOfRootProperty {
	private List<Integer> nodes;

	public void setNodes(List<Integer> nodes) {
		if (nodes == null) {
			this.nodes = nodes;
		} else if (nodes.size() < 1) {
			throw new IllegalArgumentException("Number of nodes elements is < 1");
		} else {
			Iterator var2 = nodes.iterator();

			Integer nodesElement;
			do {
				if (!var2.hasNext()) {
					this.nodes = nodes;
					return;
				}

				nodesElement = (Integer) var2.next();
			} while (nodesElement >= 0);

			throw new IllegalArgumentException("nodesElement < 0");
		}
	}

	public List<Integer> getNodes() {
		return this.nodes;
	}

	public void addNodes(Integer element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Integer> oldList = this.nodes;
			List<Integer> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.nodes = newList;
		}
	}

	public void removeNodes(Integer element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Integer> oldList = this.nodes;
			List<Integer> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.nodes = null;
			} else {
				this.nodes = newList;
			}

		}
	}
}

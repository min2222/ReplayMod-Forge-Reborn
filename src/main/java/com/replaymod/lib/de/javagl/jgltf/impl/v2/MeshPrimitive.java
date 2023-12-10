package com.replaymod.lib.de.javagl.jgltf.impl.v2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeshPrimitive extends GlTFProperty {
	private Map<String, Integer> attributes;
	private Integer indices;
	private Integer material;
	private Integer mode;
	private List<Map<String, Integer>> targets;

	public void setAttributes(Map<String, Integer> attributes) {
		if (attributes == null) {
			throw new NullPointerException("Invalid value for attributes: " + attributes + ", may not be null");
		} else {
			this.attributes = attributes;
		}
	}

	public Map<String, Integer> getAttributes() {
		return this.attributes;
	}

	public void addAttributes(String key, Integer value) {
		if (key == null) {
			throw new NullPointerException("The key may not be null");
		} else if (value == null) {
			throw new NullPointerException("The value may not be null");
		} else {
			Map<String, Integer> oldMap = this.attributes;
			Map<String, Integer> newMap = new LinkedHashMap();
			if (oldMap != null) {
				newMap.putAll(oldMap);
			}

			newMap.put(key, value);
			this.attributes = newMap;
		}
	}

	public void removeAttributes(String key) {
		if (key == null) {
			throw new NullPointerException("The key may not be null");
		} else {
			Map<String, Integer> oldMap = this.attributes;
			Map<String, Integer> newMap = new LinkedHashMap();
			if (oldMap != null) {
				newMap.putAll(oldMap);
			}

			newMap.remove(key);
			this.attributes = newMap;
		}
	}

	public void setIndices(Integer indices) {
		if (indices == null) {
			this.indices = indices;
		} else {
			this.indices = indices;
		}
	}

	public Integer getIndices() {
		return this.indices;
	}

	public void setMaterial(Integer material) {
		if (material == null) {
			this.material = material;
		} else {
			this.material = material;
		}
	}

	public Integer getMaterial() {
		return this.material;
	}

	public void setMode(Integer mode) {
		if (mode == null) {
			this.mode = mode;
		} else if (mode != 0 && mode != 1 && mode != 2 && mode != 3 && mode != 4 && mode != 5 && mode != 6) {
			throw new IllegalArgumentException("Invalid value for mode: " + mode + ", valid: [0, 1, 2, 3, 4, 5, 6]");
		} else {
			this.mode = mode;
		}
	}

	public Integer getMode() {
		return this.mode;
	}

	public Integer defaultMode() {
		return 4;
	}

	public void setTargets(List<Map<String, Integer>> targets) {
		if (targets == null) {
			this.targets = targets;
		} else if (targets.size() < 1) {
			throw new IllegalArgumentException("Number of targets elements is < 1");
		} else {
			this.targets = targets;
		}
	}

	public List<Map<String, Integer>> getTargets() {
		return this.targets;
	}

	public void addTargets(Map<String, Integer> element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Map<String, Integer>> oldList = this.targets;
			List<Map<String, Integer>> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.targets = newList;
		}
	}

	public void removeTargets(Map<String, Integer> element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Map<String, Integer>> oldList = this.targets;
			List<Map<String, Integer>> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.targets = null;
			} else {
				this.targets = newList;
			}

		}
	}
}

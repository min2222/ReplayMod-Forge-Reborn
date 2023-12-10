package com.replaymod.lib.de.javagl.jgltf.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Optionals {
	public static <T> List<T> of(List<T> list) {
		return (List) of(list, Collections.emptyList());
	}

	public static <T> T get(List<T> list, int index) {
		if (list == null) {
			return null;
		} else if (index < 0) {
			return null;
		} else {
			return index >= list.size() ? null : list.get(index);
		}
	}

	public static <K, V> Map<K, V> of(Map<K, V> map) {
		return (Map) of(map, Collections.emptyMap());
	}

	public static <T> T of(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	public static <V> V get(Object key, Map<?, V> map) {
		if (key == null) {
			return null;
		} else {
			return map == null ? null : map.get(key);
		}
	}

	public static float[] clone(float[] array) {
		return array == null ? null : (float[]) array.clone();
	}

	private Optionals() {
	}
}

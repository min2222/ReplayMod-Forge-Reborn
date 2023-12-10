package com.replaymod.lib.de.javagl.jgltf.impl.v2;

import java.util.LinkedHashMap;
import java.util.Map;

public class GlTFProperty {
	private Map<String, Object> extensions;
	private Object extras;

	public void setExtensions(Map<String, Object> extensions) {
		if (extensions == null) {
			this.extensions = extensions;
		} else {
			this.extensions = extensions;
		}
	}

	public Map<String, Object> getExtensions() {
		return this.extensions;
	}

	public void addExtensions(String key, Object value) {
		if (key == null) {
			throw new NullPointerException("The key may not be null");
		} else if (value == null) {
			throw new NullPointerException("The value may not be null");
		} else {
			Map<String, Object> oldMap = this.extensions;
			Map<String, Object> newMap = new LinkedHashMap();
			if (oldMap != null) {
				newMap.putAll(oldMap);
			}

			newMap.put(key, value);
			this.extensions = newMap;
		}
	}

	public void removeExtensions(String key) {
		if (key == null) {
			throw new NullPointerException("The key may not be null");
		} else {
			Map<String, Object> oldMap = this.extensions;
			Map<String, Object> newMap = new LinkedHashMap();
			if (oldMap != null) {
				newMap.putAll(oldMap);
			}

			newMap.remove(key);
			if (newMap.isEmpty()) {
				this.extensions = null;
			} else {
				this.extensions = newMap;
			}

		}
	}

	public void setExtras(Object extras) {
		if (extras == null) {
			this.extras = extras;
		} else {
			this.extras = extras;
		}
	}

	public Object getExtras() {
		return this.extras;
	}
}

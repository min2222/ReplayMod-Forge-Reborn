package com.replaymod.core;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.replaymod.core.events.SettingsChangedCallback;

public class SettingsRegistry {
	private final Map<SettingKey<?>, Object> settings = Collections.synchronizedMap(new LinkedHashMap<>());
	final SettingsRegistryBackend backend;

	public SettingsRegistry() {
		this.backend = new SettingsRegistryBackend(this.settings);
	}

	public void register() {
		this.backend.register();
	}

	public void register(Class<?> settingsClass) {
		Field[] var2 = settingsClass.getDeclaredFields();
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			Field field = var2[var4];
			if ((field.getModifiers() & 9) != 0
					&& SettingsRegistry.SettingKey.class.isAssignableFrom(field.getType())) {
				try {
					this.register((SettingsRegistry.SettingKey) field.get((Object) null));
				} catch (IllegalAccessException var7) {
					var7.printStackTrace();
				}
			}
		}

	}

	public void register(SettingsRegistry.SettingKey<?> key) {
		this.settings.put(key, key.getDefault());
		this.backend.register(key);
	}

	public Set<SettingsRegistry.SettingKey<?>> getSettings() {
		return this.settings.keySet();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(SettingKey<T> key) {
		if (!settings.containsKey(key)) {
			throw new IllegalArgumentException("Setting " + key + " unknown.");
		}
		return (T) settings.get(key);
	}

	public <T> void set(SettingsRegistry.SettingKey<T> key, T value) {
		this.backend.update(key, value);
		this.settings.put(key, value);
		((SettingsChangedCallback) SettingsChangedCallback.EVENT.invoker()).onSettingsChanged(this, key);
	}

	public void save() {
		this.backend.save();
	}

	public interface SettingKey<T> {
		String getCategory();

		String getKey();

		String getDisplayString();

		T getDefault();
	}

	public static class MultipleChoiceSettingKeys<T> extends SettingsRegistry.SettingKeys<T>
			implements SettingsRegistry.MultipleChoiceSettingKey<T> {
		private List<T> choices = Collections.emptyList();

		public MultipleChoiceSettingKeys(String category, String key, String displayString, T defaultValue) {
			super(category, key, displayString, defaultValue);
		}

		public void setChoices(List<T> choices) {
			this.choices = Collections.unmodifiableList(choices);
		}

		public List<T> getChoices() {
			return this.choices;
		}
	}

	public static class SettingKeys<T> implements SettingsRegistry.SettingKey<T> {
		private final String category;
		private final String key;
		private final String displayString;
		private final T defaultValue;

		public SettingKeys(String category, String key, String displayString, T defaultValue) {
			this.category = category;
			this.key = key;
			this.displayString = displayString;
			this.defaultValue = defaultValue;
		}

		public String getCategory() {
			return this.category;
		}

		public String getKey() {
			return this.key;
		}

		public String getDisplayString() {
			return this.displayString;
		}

		public T getDefault() {
			return this.defaultValue;
		}
	}

	public interface MultipleChoiceSettingKey<T> extends SettingsRegistry.SettingKey<T> {
		List<T> getChoices();
	}
}

package com.replaymod.simplepathing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.replaymod.core.SettingsRegistry;

public final class Setting<T> extends SettingsRegistry.SettingKeys<T> {
	public static final Setting<Boolean> PATH_PREVIEW = make("pathpreview", "pathpreview", true);
	public static final Setting<Boolean> AUTO_SYNC = make("autosync", (String) null, true);
	public static final Setting<Integer> TIMELINE_LENGTH = make("timelineLength", (String) null, 1800);
	public static final SettingsRegistry.MultipleChoiceSettingKeys<String> DEFAULT_INTERPOLATION;

	private static <T> Setting<T> make(String key, String displayName, T defaultValue) {
		return new Setting(key, displayName, defaultValue);
	}

	public Setting(String key, String displayString, T defaultValue) {
		super("simplepathing", key, displayString == null ? null : "replaymod.gui.settings." + displayString,
				defaultValue);
	}

	static {
		String format = "replaymod.gui.editkeyframe.interpolator.%s.name";
		DEFAULT_INTERPOLATION = new SettingsRegistry.MultipleChoiceSettingKeys("simplepathing", "interpolator",
				"replaymod.gui.settings.interpolator",
				String.format(format, InterpolatorType.fromString("invalid returns default").getLocalizationKey()));
		DEFAULT_INTERPOLATION.setChoices((List) Arrays.stream(InterpolatorType.values()).filter((i) -> {
			return i != InterpolatorType.DEFAULT;
		}).map((i) -> {
			return String.format(format, i.getLocalizationKey());
		}).collect(Collectors.toList()));
	}
}

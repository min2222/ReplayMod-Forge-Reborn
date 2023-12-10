package com.replaymod.replay;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.replaymod.core.SettingsRegistry;
import com.replaymod.replay.handler.GuiHandler;

public final class Setting<T> extends SettingsRegistry.SettingKeys<T> {
	public static final Setting<Boolean> SHOW_CHAT = make("showChat", "showchat", true);
	public static final Setting<Boolean> SHOW_SERVER_IPS = new Setting("showServerIPs", true);
	public static final SettingsRegistry.MultipleChoiceSettingKeys<String> CAMERA = new SettingsRegistry.MultipleChoiceSettingKeys(
			"replay", "camera", "replaymod.gui.settings.camera", "replaymod.camera.classic");
	public static final Setting<Boolean> LEGACY_MAIN_MENU_BUTTON = new Setting("legacyMainMenuButton", false);
	public static final SettingsRegistry.MultipleChoiceSettingKeys<String> MAIN_MENU_BUTTON;

	private static <T> Setting<T> make(String key, String displayName, T defaultValue) {
		return new Setting(key, displayName, defaultValue);
	}

	public Setting(String key, String displayString, T defaultValue) {
		super("replay", key, "replaymod.gui.settings." + displayString, defaultValue);
	}

	public Setting(String key, T defaultValue) {
		super("replay", key, (String) null, defaultValue);
	}

	static {
		MAIN_MENU_BUTTON = new SettingsRegistry.MultipleChoiceSettingKeys("replay", "mainMenuButton", (String) null,
				GuiHandler.MainMenuButtonPosition.DEFAULT.name());
		MAIN_MENU_BUTTON.setChoices((List) Arrays.stream(GuiHandler.MainMenuButtonPosition.values()).map(Enum::name)
				.collect(Collectors.toList()));
	}
}

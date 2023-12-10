package com.replaymod.recording.handler;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.gui.GuiReplayButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiCheckbox;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiToggleButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiInfoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.recording.ServerInfoExt;
import com.replaymod.recording.Setting;
import com.replaymod.replay.mixin.AddServerScreenAccessor;

import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;

public class GuiHandler extends EventRegistrations {
	private final ReplayMod mod;

	public GuiHandler(ReplayMod mod) {
		this.on(InitScreenCallback.EVENT, (screen, buttons) -> {
			this.onGuiInit(screen);
		});
		this.mod = mod;
	}

	private void onGuiInit(Screen gui) {
		if (gui instanceof SelectWorldScreen || gui instanceof JoinMultiplayerScreen) {
			boolean sp = gui instanceof SelectWorldScreen;
			SettingsRegistry settingsRegistry = this.mod.getSettingsRegistry();
			Setting<Boolean> setting = sp ? Setting.RECORD_SINGLEPLAYER : Setting.RECORD_SERVER;
			final GuiCheckbox recordingCheckbox = (GuiCheckbox) ((GuiCheckbox) (new GuiCheckbox())
					.setI18nLabel("replaymod.gui.settings.record" + (sp ? "singleplayer" : "server"), new Object[0]))
					.setChecked((Boolean) settingsRegistry.get(setting));
			recordingCheckbox.onClick(() -> {
				settingsRegistry.set(setting, recordingCheckbox.isChecked());
				settingsRegistry.save();
			});
			VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(gui);
			((GuiScreen) vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
				protected void layout(GuiScreen container, int width, int height) {
					this.pos(recordingCheckbox, width - this.width(recordingCheckbox) - 5, 5);
				}
			})).addElements((LayoutData) null, new GuiElement[] { recordingCheckbox });
		}

		if (gui instanceof EditServerScreen) {
			VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(gui);
			final GuiButton replayButton = (GuiButton) (new GuiReplayButton()).onClick(() -> {
				ServerData serverInfo = ((AddServerScreenAccessor) gui).getServer();
				ServerInfoExt serverInfoExt = ServerInfoExt.from(serverInfo);
				Boolean state = serverInfoExt.getAutoRecording();
				GuiToggleButton<String> autoRecording = (GuiToggleButton) ((GuiToggleButton) ((GuiToggleButton) (new GuiToggleButton())
						.setI18nLabel("replaymod.gui.settings.autostartrecording", new Object[0]))
						.setValues(new String[] { I18n.get("replaymod.gui.settings.default", new Object[0]),
								I18n.get("options.off", new Object[0]), I18n.get("options.on", new Object[0]) }))
						.setSelected(state == null ? 0 : (state ? 2 : 1));
				autoRecording.onClick(() -> {
					int selected = autoRecording.getSelected();
					serverInfoExt.setAutoRecording(selected == 0 ? null : selected == 2);
				});
				GuiInfoPopup.open(vanillaGui, (autoRecording));
			});
			((GuiScreen) vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
				protected void layout(GuiScreen container, int width, int height) {
					this.size(replayButton, 20, 20);
					this.pos(replayButton, width - this.width(replayButton) - 5, 5);
				}
			})).addElements((LayoutData) null, new GuiElement[] { replayButton });
		}

	}
}

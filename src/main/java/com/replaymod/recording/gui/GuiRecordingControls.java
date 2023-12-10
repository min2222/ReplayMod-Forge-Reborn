package com.replaymod.recording.gui;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.recording.packet.PacketListener;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;

public class GuiRecordingControls extends EventRegistrations {
	private ReplayMod core;
	private PacketListener packetListener;
	private boolean paused;
	private boolean stopped;
	private GuiPanel panel = (GuiPanel) (new GuiPanel()).setLayout((new HorizontalLayout()).setSpacing(4));
	private GuiButton buttonPauseResume;
	private GuiButton buttonStartStop;

	public GuiRecordingControls(ReplayMod core, PacketListener packetListener, boolean autoStart) {
		this.buttonPauseResume = (GuiButton) ((GuiButton) (new GuiButton(this.panel)).onClick(() -> {
			if (!Utils.ifMinimalModeDoPopup(this.panel, () -> {
			})) {
				if (this.paused) {
					this.packetListener.addMarker("_RM_END_CUT");
				} else {
					this.packetListener.addMarker("_RM_START_CUT");
				}

				this.paused = !this.paused;
				this.updateState();
			}
		})).setSize(98, 20);
		this.buttonStartStop = (GuiButton) ((GuiButton) (new GuiButton(this.panel)).onClick(() -> {
			if (!Utils.ifMinimalModeDoPopup(this.panel, () -> {
			})) {
				if (this.stopped) {
					this.paused = false;
					this.packetListener.addMarker("_RM_END_CUT");
					this.core.printInfoToChat("replaymod.chat.recordingstarted");
				} else {
					int timestamp = (int) this.packetListener.getCurrentDuration();
					if (!this.paused) {
						this.packetListener.addMarker("_RM_START_CUT", timestamp);
					}

					this.packetListener.addMarker("_RM_SPLIT", timestamp + 1);
				}

				this.stopped = !this.stopped;
				this.updateState();
			}
		})).setSize(98, 20);
		this.on(InitScreenCallback.EVENT, this::injectIntoIngameMenu);
		this.core = core;
		this.packetListener = packetListener;
		this.paused = this.stopped = !autoStart;
		this.updateState();
	}

	private void updateState() {
		String var10001 = this.paused ? "resume" : "pause";
		this.buttonPauseResume.setI18nLabel("replaymod.gui.recording." + var10001, new Object[0]);
		var10001 = this.stopped ? "start" : "stop";
		this.buttonStartStop.setI18nLabel("replaymod.gui.recording." + var10001, new Object[0]);
		this.buttonPauseResume.setEnabled(!this.stopped);
	}

	private void injectIntoIngameMenu(Screen guiScreen, Collection<AbstractWidget> buttonList) {
		if (!(guiScreen instanceof PauseScreen)) {
			return;
		}
		if (buttonList.isEmpty()) {
			return; // menu-less pause (F3+Esc)
		}
		Function<Integer, Integer> yPos = MCVer.findButton(buttonList, "menu.returnToMenu", 1).map(Optional::of)
				.orElse(MCVer.findButton(buttonList, "menu.disconnect", 1))
				.<Function<Integer, Integer>>map(it -> (height) -> it.y).orElse((height) -> height / 4 + 120 - 16);
		VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(guiScreen);
		vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
			@Override
			protected void layout(GuiScreen container, int width, int height) {
				pos(panel, width / 2 - 100, yPos.apply(height) + 16 + 8);
			}
		}).addElements(null, panel);
	}

	public boolean isPaused() {
		return this.paused;
	}

	public boolean isStopped() {
		return this.stopped;
	}
}

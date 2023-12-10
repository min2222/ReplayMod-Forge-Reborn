package com.replaymod.extras.advancedscreenshots;

import com.replaymod.core.ReplayMod;
import com.replaymod.extras.Extra;

public class AdvancedScreenshots implements Extra {
	private ReplayMod mod;
	private static AdvancedScreenshots instance;

	public AdvancedScreenshots() {
		instance = this;
	}

	public void register(ReplayMod mod) {
		this.mod = mod;
	}

	public static void take() {
		if (instance != null) {
			instance.takeScreenshot();
		}

	}

	private void takeScreenshot() {
		ReplayMod.instance.runLater(() -> {
			(new GuiCreateScreenshot(this.mod)).open();
		});
	}
}

package com.replaymod.simplepathing.preview;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.events.SettingsChangedCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.events.ReplayClosedCallback;
import com.replaymod.replay.events.ReplayOpenedCallback;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import com.replaymod.simplepathing.Setting;

public class PathPreview extends EventRegistrations {
	private final ReplayModSimplePathing mod;
	private ReplayHandler replayHandler;
	private PathPreviewRenderer renderer;

	public PathPreview(ReplayModSimplePathing mod) {
		this.mod = mod;
		this.on(SettingsChangedCallback.EVENT, (registry, key) -> {
			if (key == Setting.PATH_PREVIEW) {
				this.update();
			}

		});
		this.on(ReplayOpenedCallback.EVENT, (replayHandler) -> {
			this.replayHandler = replayHandler;
			this.update();
		});
		this.on(ReplayClosedCallback.EVENT, (replayHandler) -> {
			this.replayHandler = null;
			this.update();
		});
	}

	public void registerKeyBindings(KeyBindingRegistry registry) {
		registry.registerKeyBinding("replaymod.input.pathpreview", 72, () -> {
			SettingsRegistry settings = this.mod.getCore().getSettingsRegistry();
			settings.set(Setting.PATH_PREVIEW, !(Boolean) settings.get(Setting.PATH_PREVIEW));
			settings.save();
		}, true);
	}

	private void update() {
		if ((Boolean) this.mod.getCore().getSettingsRegistry().get(Setting.PATH_PREVIEW)
				&& this.replayHandler != null) {
			if (this.renderer == null) {
				this.renderer = new PathPreviewRenderer(this.mod, this.replayHandler);
				this.renderer.register();
			}
		} else if (this.renderer != null) {
			this.renderer.unregister();
			this.renderer = null;
		}

	}
}

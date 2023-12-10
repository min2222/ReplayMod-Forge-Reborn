package com.replaymod.extras;

import com.replaymod.core.ReplayMod;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiImage;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.events.ReplayOpenedCallback;
import com.replaymod.replay.gui.overlay.GuiReplayOverlay;

public class QuickMode extends EventRegistrations implements Extra {
	private ReplayModReplay module;
	private final GuiImage indicator;

	public QuickMode() {
		this.indicator = (GuiImage) ((GuiImage) (new GuiImage()).setTexture(ReplayMod.TEXTURE, 40, 100, 16, 16))
				.setSize(16, 16);
		this.on(ReplayOpenedCallback.EVENT, (replayHandler) -> {
			this.updateIndicator(replayHandler.getOverlay(), replayHandler.isQuickMode());
		});
	}

	public void register(ReplayMod mod) {
		this.module = ReplayModReplay.instance;
		mod.getKeyBindingRegistry().registerKeyBinding("replaymod.input.quickmode", 81, () -> {
			ReplayHandler replayHandler = this.module.getReplayHandler();
			if (replayHandler != null) {
				replayHandler.getReplaySender().setSyncModeAndWait();
				mod.runLaterWithoutLock(() -> {
					replayHandler.ensureQuickModeInitialized(() -> {
						boolean enabled = !replayHandler.isQuickMode();
						this.updateIndicator(replayHandler.getOverlay(), enabled);
						replayHandler.setQuickMode(enabled);
						replayHandler.getReplaySender().setAsyncMode(true);
					});
				});
			}
		}, true);
		this.register();
	}

	private void updateIndicator(GuiReplayOverlay overlay, boolean enabled) {
		if (enabled) {
			overlay.statusIndicatorPanel.addElements(new HorizontalLayout.Data(1.0D),
					new GuiElement[] { this.indicator });
		} else {
			overlay.statusIndicatorPanel.removeElement(this.indicator);
		}

	}
}

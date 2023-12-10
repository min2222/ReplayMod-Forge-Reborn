package com.replaymod.extras.youtube;

import com.replaymod.core.ReplayMod;
import com.replaymod.extras.Extra;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.render.gui.GuiRenderingDone;

import net.minecraft.client.gui.screens.Screen;

public class YoutubeUpload extends EventRegistrations implements Extra {
	public YoutubeUpload() {
		this.on(InitScreenCallback.EVENT, (screen, buttons) -> {
			this.onGuiOpen(screen);
		});
	}

	public void register(ReplayMod mod) {
		this.register();
	}

	private void onGuiOpen(Screen vanillaGui) {
		AbstractGuiScreen<?> abstractScreen = GuiScreen.from(vanillaGui);
		if (abstractScreen instanceof GuiRenderingDone) {
			GuiRenderingDone gui = (GuiRenderingDone) abstractScreen;
			if (gui.actionsPanel.getChildren().stream().anyMatch((it) -> {
				return it instanceof YoutubeUpload.YoutubeButton;
			})) {
				return;
			}

			gui.actionsPanel.addElements((LayoutData) null,
					new GuiElement[] { ((GuiButton) ((GuiButton) (new YoutubeUpload.YoutubeButton()).onClick(() -> {
						(new GuiYoutubeUpload(gui, gui.videoFile, gui.videoFrames, gui.settings)).display();
					})).setSize(200, 20)).setI18nLabel("replaymod.gui.youtubeupload", new Object[0]) });
		}

	}

	private static class YoutubeButton extends GuiButton {
	}
}

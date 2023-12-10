package com.replaymod.editor.handler;

import java.io.IOException;

import com.replaymod.core.utils.Utils;
import com.replaymod.editor.ReplayModEditor;
import com.replaymod.editor.gui.GuiEditReplay;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.replay.gui.screen.GuiReplayViewer;

import net.minecraft.CrashReport;
import net.minecraft.client.gui.screens.Screen;

public class GuiHandler extends EventRegistrations {
	public GuiHandler() {
		this.on(InitScreenCallback.EVENT, (vanillaGuiScreen, buttonList) -> {
			this.injectIntoReplayViewer(vanillaGuiScreen);
		});
	}

	public void injectIntoReplayViewer(Screen vanillaGuiScreen) {
		AbstractGuiScreen guiScreen = GuiScreen.from(vanillaGuiScreen);
		if (guiScreen instanceof GuiReplayViewer) {
			GuiReplayViewer replayViewer = (GuiReplayViewer) guiScreen;
			if (replayViewer.editorButton.getChildren().isEmpty()) {
				replayViewer.replaySpecificButtons.add(
						(GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton(replayViewer.editorButton))
								.onClick(() -> {
									if (!Utils.ifMinimalModeDoPopup(replayViewer, () -> {
									})) {
										try {
											(new GuiEditReplay(replayViewer,
													((GuiReplayViewer.GuiReplayEntry) replayViewer.list.getSelected()
															.get(0)).file.toPath()) {
												protected void close() {
													super.close();
													replayViewer.list.load();
												}
											}).open();
										} catch (IOException var3) {
											Utils.error(ReplayModEditor.LOGGER, replayViewer,
													CrashReport.forThrowable(var3, "Opening replay editor"), () -> {
													});
										}

									}
								})).setSize(73, 20)).setI18nLabel("replaymod.gui.edit", new Object[0])).setDisabled());
			}
		}
	}
}

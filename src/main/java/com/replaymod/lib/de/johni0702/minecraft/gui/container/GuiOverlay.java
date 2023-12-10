package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import net.minecraft.client.gui.screens.Screen;

public class GuiOverlay extends AbstractGuiOverlay<GuiOverlay> {
	public static AbstractGuiOverlay from(Screen minecraft) {
		return !(minecraft instanceof AbstractGuiOverlay.UserInputGuiScreen) ? null
				: ((AbstractGuiOverlay.UserInputGuiScreen) minecraft).getOverlay();
	}

	protected GuiOverlay getThis() {
		return this;
	}
}

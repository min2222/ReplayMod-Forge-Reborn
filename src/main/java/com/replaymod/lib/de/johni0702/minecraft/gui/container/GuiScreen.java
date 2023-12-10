package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import net.minecraft.client.gui.screens.Screen;

public class GuiScreen extends AbstractGuiScreen<GuiScreen> {
	public static AbstractGuiScreen from(Screen minecraft) {
		return !(minecraft instanceof AbstractGuiScreen.MinecraftGuiScreen) ? null
				: ((AbstractGuiScreen.MinecraftGuiScreen) minecraft).getWrapper();
	}

	public static GuiScreen wrap(Screen minecraft) {
		return new GuiScreen() {
			public Screen toMinecraft() {
				return minecraft;
			}
		};
	}

	protected GuiScreen getThis() {
		return this;
	}
}

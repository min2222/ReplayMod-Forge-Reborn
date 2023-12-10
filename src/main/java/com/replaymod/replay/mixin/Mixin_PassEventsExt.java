package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.ScreenExt;

import net.minecraft.client.gui.screens.Screen;

@Mixin({ Screen.class })
public abstract class Mixin_PassEventsExt implements ScreenExt {
	@Shadow
	public boolean passEvents;

	public boolean doesPassEvents() {
		return this.passEvents;
	}

	public void setPassEvents(boolean passEvents) {
		this.passEvents = passEvents;
	}
}

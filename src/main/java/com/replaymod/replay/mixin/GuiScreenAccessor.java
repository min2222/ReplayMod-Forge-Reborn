package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.screens.Screen;

@Mixin({ Screen.class })
public interface GuiScreenAccessor {
	/*
	 * @Invoker("addRenderableWidget") <T extends GuiEventListener &
	 * NarratableEntry> T invokeAddButton(T element);
	 */
}

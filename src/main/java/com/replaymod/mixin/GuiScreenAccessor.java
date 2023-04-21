package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public interface GuiScreenAccessor
{
	//TODO
    /*@Invoker("addRenderableWidget")
    <T extends GuiEventListener & Widget & NarratableEntry> T invokeAddButton(T p_96625_);*/
}

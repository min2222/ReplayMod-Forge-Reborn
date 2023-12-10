package com.replaymod.gui.versions.forge;

import java.util.Collection;

import com.google.common.collect.Collections2;
import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.OpenGuiScreenCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PostRenderScreenCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.RenderHudCallback;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventsAdapter extends EventRegistrations {
	public static Screen getScreen(ScreenEvent event) {
		return event.getScreen();
	}

	public static Collection<AbstractWidget> getButtonList(ScreenEvent.Init event) {
		return Collections2.transform(Collections2.filter(event.getListenersList(), it -> it instanceof AbstractWidget),
				it -> (AbstractWidget) it);
	}

	@SubscribeEvent
	public void preGuiInit(ScreenEvent.Init.Pre event) {
		InitScreenCallback.Pre.EVENT.invoker().preInitScreen(getScreen(event));
	}

	@SubscribeEvent
	public void onGuiInit(ScreenEvent.Init.Post event) {
		InitScreenCallback.EVENT.invoker().initScreen(getScreen(event), getButtonList(event));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onGuiClosed(ScreenEvent.Opening event) {
		OpenGuiScreenCallback.EVENT.invoker().openGuiScreen(event.getScreen());
	}

	public static float getPartialTicks(CustomizeGuiOverlayEvent event) {
		return event.getPartialTick();
	}

	public static float getPartialTicks(ScreenEvent.Render.Post event) {
		return event.getPartialTick();
	}

	@SubscribeEvent
	public void onGuiRender(ScreenEvent.Render.Post event) {
		PostRenderScreenCallback.EVENT.invoker().postRenderScreen(new PoseStack(), getPartialTicks(event));
	}

	// Even when event was cancelled cause Lunatrius' InGame-Info-XML mod cancels it
	// and we don't actually care about
	// the event (i.e. the overlay text), just about when it's called.
	@SubscribeEvent(receiveCanceled = true)
	public void renderOverlay(CustomizeGuiOverlayEvent.DebugText event) {
		RenderHudCallback.EVENT.invoker().renderHud(new PoseStack(), getPartialTicks(event));
	}

	@SubscribeEvent
	public void tickOverlay(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			PreTickCallback.EVENT.invoker().preTick();
		}
	}
}
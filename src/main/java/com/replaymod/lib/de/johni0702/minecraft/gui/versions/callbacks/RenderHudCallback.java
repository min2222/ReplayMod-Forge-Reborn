package com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface RenderHudCallback {
	Event<RenderHudCallback> EVENT = Event.create((listeners) -> (stack, partialTicks) -> {
		for (RenderHudCallback listener : listeners) {
			listener.renderHud(stack, partialTicks);
		}
	});

	// #if MC>=12000
	// $$ void renderHud(DrawContext context, float partialTicks);
	// #else
	void renderHud(PoseStack stack, float partialTicks);
	// #endif
}

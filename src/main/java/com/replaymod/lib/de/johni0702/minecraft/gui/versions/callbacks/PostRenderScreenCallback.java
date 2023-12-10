package com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface PostRenderScreenCallback {
	Event<PostRenderScreenCallback> EVENT = Event.create((listeners) -> (stack, partialTicks) -> {
		for (PostRenderScreenCallback listener : listeners) {
			listener.postRenderScreen(stack, partialTicks);
		}
	});

	// #if MC>=12000
	// $$ void postRenderScreen(DrawContext context, float partialTicks);
	// #else
	void postRenderScreen(PoseStack stack, float partialTicks);
	// #endif
}

package com.replaymod.core.events;

import java.util.Iterator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface PostRenderWorldCallback {
	Event<PostRenderWorldCallback> EVENT = Event.create((listeners) -> {
		return (matrixStack) -> {
			Iterator var2 = listeners.iterator();

			while (var2.hasNext()) {
				PostRenderWorldCallback listener = (PostRenderWorldCallback) var2.next();
				listener.postRenderWorld(matrixStack);
			}

		};
	});

	void postRenderWorld(PoseStack matrixStack);
}

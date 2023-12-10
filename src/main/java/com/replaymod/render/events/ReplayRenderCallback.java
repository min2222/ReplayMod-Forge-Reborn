package com.replaymod.render.events;

import java.util.Iterator;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.render.rendering.VideoRenderer;

public interface ReplayRenderCallback {
	public interface Post {
		Event<ReplayRenderCallback.Post> EVENT = Event.create((listeners) -> {
			return (renderer) -> {
				Iterator var2 = listeners.iterator();

				while (var2.hasNext()) {
					ReplayRenderCallback.Post listener = (ReplayRenderCallback.Post) var2.next();
					listener.afterRendering(renderer);
				}

			};
		});

		void afterRendering(VideoRenderer videoRenderer);
	}

	public interface Pre {
		Event<ReplayRenderCallback.Pre> EVENT = Event.create((listeners) -> {
			return (renderer) -> {
				Iterator var2 = listeners.iterator();

				while (var2.hasNext()) {
					ReplayRenderCallback.Pre listener = (ReplayRenderCallback.Pre) var2.next();
					listener.beforeRendering(renderer);
				}

			};
		});

		void beforeRendering(VideoRenderer videoRenderer);
	}
}

package com.replaymod.gui.versions.callbacks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.gui.utils.Event;

public interface PostRenderScreenCallback {
    Event<PostRenderScreenCallback> EVENT = Event.create((listeners) ->
            (stack, partialTicks) -> {
                for (PostRenderScreenCallback listener : listeners) {
                    listener.postRenderScreen(stack, partialTicks);
                }
            }
    );

    void postRenderScreen(PoseStack stack, float partialTicks);
}

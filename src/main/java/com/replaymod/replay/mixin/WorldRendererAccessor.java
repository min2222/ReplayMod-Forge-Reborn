package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.replaymod.render.capturer.WorldRenderer;

@Mixin({ WorldRenderer.class })
public interface WorldRendererAccessor {
}

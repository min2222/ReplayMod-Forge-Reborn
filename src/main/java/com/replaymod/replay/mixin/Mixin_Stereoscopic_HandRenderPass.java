package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.GameRenderer;

@Mixin({ GameRenderer.class })
public abstract class Mixin_Stereoscopic_HandRenderPass {
}

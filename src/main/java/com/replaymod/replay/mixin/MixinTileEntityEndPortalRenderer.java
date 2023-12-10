package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.mojang.blaze3d.systems.RenderSystem;

@Mixin({ RenderSystem.class })
public class MixinTileEntityEndPortalRenderer {
}

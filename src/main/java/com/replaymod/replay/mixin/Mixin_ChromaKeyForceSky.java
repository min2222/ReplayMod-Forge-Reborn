package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin({ LevelRenderer.class })
public abstract class Mixin_ChromaKeyForceSky {
	@Shadow
	@Final
	private Minecraft minecraft;
}

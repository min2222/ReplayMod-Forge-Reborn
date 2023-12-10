package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.GameRenderer;

@Mixin({ GameRenderer.class })
public interface GameRendererAccessor {
	@Accessor("renderHand")
	boolean getRenderHand();

	@Accessor("renderHand")
	void setRenderHand(boolean bl);
}

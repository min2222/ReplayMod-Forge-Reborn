package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = { "net/optifine/render/ChunkVisibility" }, remap = false)
public abstract class MixinChunkVisibility {
	@Shadow
	private static int counter;

	@Inject(method = { "reset" }, at = { @At("HEAD") }, remap = false)
	private static void replayModCompat_fixImproperReset(CallbackInfo ci) {
		counter = 0;
	}
}

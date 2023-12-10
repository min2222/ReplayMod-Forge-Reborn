package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.replaymod.replay.InputReplayTimer;
import com.replaymod.replay.ReplayModReplay;

import net.minecraft.client.MouseHandler;

@Mixin({ MouseHandler.class })
public abstract class MixinMouseHelper {
	@Shadow
	private boolean mouseGrabbed;

	@Inject(method = { "grabMouse" }, at = { @At("HEAD") }, cancellable = true)
	private void noGrab(CallbackInfo ci) {
		if (Boolean.valueOf(System.getProperty("fml.noGrab", "false"))) {
			this.mouseGrabbed = true;
			ci.cancel();
		}

	}

	@Inject(method = { "onScroll" }, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z") }, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void handleReplayModScroll(long _p0, double _p1, double _p2, CallbackInfo ci, double yOffsetAccumulated) {
		if (ReplayModReplay.instance.getReplayHandler() != null) {
			InputReplayTimer.handleScroll((int) (yOffsetAccumulated * 120));
			ci.cancel();
		}

	}
}

package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.compat.shaders.ShaderReflection;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;

import net.minecraft.client.renderer.GameRenderer;

@Mixin({ GameRenderer.class })
public abstract class MixinShaderEntityRenderer {
	@Inject(method = { "renderWorld" }, at = { @At("HEAD") })
	private void replayModCompat_updateShaderFrameTimeCounter(CallbackInfo ignore) {
		if (ReplayModReplay.instance.getReplayHandler() != null) {
			if (ShaderReflection.shaders_frameTimeCounter != null) {
				ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
				float timestamp = (float) replayHandler.getReplaySender().currentTimeStamp() / 1000.0F % 3600.0F;

				try {
					ShaderReflection.shaders_frameTimeCounter.set((Object) null, timestamp);
				} catch (Exception var5) {
					var5.printStackTrace();
				}

			}
		}
	}
}

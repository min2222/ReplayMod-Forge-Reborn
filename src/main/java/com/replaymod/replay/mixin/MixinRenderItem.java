package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;

@Mixin({ RenderStateShard.class })
public class MixinRenderItem {
	@Redirect(method = {
			"setupGlintTexturing" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"))
	private static long getEnchantmentTime() {
		ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
		return replayHandler != null ? (long) replayHandler.getReplaySender().currentTimeStamp() : Util.getMillis();
	}
}

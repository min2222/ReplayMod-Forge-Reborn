package com.replaymod.replay.mixin.world_border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;

@Mixin(targets = { "net.minecraft.world.level.border.WorldBorder$MovingBorderExtent" })
public class Mixin_UseReplayTime_ForMovement {
	@Redirect(method = { "*" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"))
	private long getWorldBorderTime() {
		ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
		return replayHandler != null ? (long) replayHandler.getReplaySender().currentTimeStamp() : MCVer.milliTime();
	}
}

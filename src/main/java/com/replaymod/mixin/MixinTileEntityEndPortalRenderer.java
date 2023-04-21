package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.RenderStateShard;

@Mixin(RenderStateShard.class)
public class MixinTileEntityEndPortalRenderer {
	//TODO
	//net.minecraft.client.renderer.RenderState.PortalTexturingState
    /*@Redirect(method = "func_228597_a_", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;milliTime()J"))
    static
    private long replayModReplay_getEnchantmentTime() {
        ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
        if (replayHandler != null) {
            return replayHandler.getReplaySender().currentTimeStamp();
        }
        return Util.getMillis();
    }*/
}

package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.core.versions.MCVer;
import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.renderer.FogRenderer;

@Mixin({ FogRenderer.class })
public abstract class Mixin_ChromaKeyDisableFog {
	@Inject(method = { "setupFog" }, at = { @At("HEAD") }, cancellable = true)
	private static void replayModRender_onSetupFog(CallbackInfo ci) {
		EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) MCVer.getMinecraft().gameRenderer)
				.replayModRender_getHandler();
		if (handler != null) {
			if (handler.getSettings().getChromaKeyingColor() != null) {
				RenderSystem.setShaderFogStart(1.0E10F);
				RenderSystem.setShaderFogEnd(2.0E10F);
				ci.cancel();
			}

		}
	}
}

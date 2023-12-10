package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.versions.MCVer;
import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.renderer.entity.EntityRenderer;

@Mixin({ EntityRenderer.class })
public abstract class Mixin_HideNameTags {
	@Inject(method = { "renderNameTag" }, at = { @At("HEAD") }, cancellable = true)
	private void replayModRender_areAllNamesHidden(CallbackInfo ci) {
		EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) MCVer.getMinecraft().gameRenderer)
				.replayModRender_getHandler();
		if (handler != null && !handler.getSettings().isRenderNameTags()) {
			ci.cancel();
		}

	}
}

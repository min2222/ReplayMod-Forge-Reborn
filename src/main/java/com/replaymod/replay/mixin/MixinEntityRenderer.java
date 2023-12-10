package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.renderer.GameRenderer;

@Mixin({ GameRenderer.class })
public abstract class MixinEntityRenderer implements EntityRendererHandler.IEntityRenderer {
	private EntityRendererHandler replayModRender_handler;

	public void replayModRender_setHandler(EntityRendererHandler handler) {
		this.replayModRender_handler = handler;
	}

	public EntityRendererHandler replayModRender_getHandler() {
		return this.replayModRender_handler;
	}
}

package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.replaymod.replay.ReplayModReplay;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Entity;

@Mixin({ ArrowRenderer.class })
public abstract class MixinRenderArrow extends EntityRenderer {
	protected MixinRenderArrow(Context context) {
		super(context);
	}

	public boolean shouldRender(Entity entity, Frustum camera, double camX, double camY, double camZ) {
		return ReplayModReplay.instance.getReplayHandler() != null
				|| super.shouldRender(entity, camera, camX, camY, camZ);
	}
}

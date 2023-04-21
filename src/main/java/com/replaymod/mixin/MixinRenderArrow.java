package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.replaymod.replay.ReplayModReplay;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.world.entity.Entity;

@Mixin(TippableArrowRenderer.class)
public abstract class MixinRenderArrow extends EntityRenderer {
    protected MixinRenderArrow(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean shouldRender(Entity entity,
                                Frustum camera,
                                double camX, double camY, double camZ) {
        // Force arrows to always render, otherwise they stop rendering when you get close to them
        return ReplayModReplay.instance.getReplayHandler() != null || super.shouldRender(entity, camera, camX, camY, camZ);
    }
}

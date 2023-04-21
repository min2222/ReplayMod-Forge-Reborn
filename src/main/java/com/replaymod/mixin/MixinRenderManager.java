package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.replaymod.core.versions.MCVer;
import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinRenderManager {
    @Shadow
    private Quaternion cameraOrientation;

    @Inject(method = "render", at = @At("HEAD"))
    private void replayModRender_reorientForCubicRendering(Entity entity, double dx, double dy, double dz, float iDoNotKnow, float partialTicks,
                                                           PoseStack matrixStack,
                                                           MultiBufferSource vertexConsumerProvider,
                                                           int int_1,
                                                           CallbackInfo ci) {
        EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) MCVer.getMinecraft().gameRenderer).replayModRender_getHandler();
        if (handler != null && handler.omnidirectional) {
            double pitch = -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
            double yaw = -Math.atan2(dx, dz);
            this.cameraOrientation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
            this.cameraOrientation.mul(Vector3f.YP.rotationDegrees((float) -yaw));
            this.cameraOrientation.mul(Vector3f.XP.rotationDegrees((float) pitch));
        }
    }
}

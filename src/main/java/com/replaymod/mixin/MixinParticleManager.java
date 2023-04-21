package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.replaymod.core.versions.MCVer;
import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.phys.Vec3;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleManager
{
    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"))
    private void buildOrientedGeometry(Particle particle, VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) MCVer.getMinecraft().gameRenderer).replayModRender_getHandler();
        if (handler == null || !handler.omnidirectional) {
            buildGeometry(particle, vertexConsumer, camera, partialTicks);
        } else {
            Quaternion rotation = camera.rotation();
            Quaternion org = rotation.copy();
            try {
            	Vec3 from = new Vec3(0, 0, 1);
                Vec3 to = MCVer.getPosition(particle, partialTicks).subtract(camera.getPosition()).normalize();
                Vec3 axis = from.cross(to);
                rotation.set((float) axis.x, (float) axis.y, (float) axis.z, (float) (1 + from.dot(to)));
                rotation.normalize();

                buildGeometry(particle, vertexConsumer, camera, partialTicks);
            } finally {
                rotation.set(org.r(), org.i(), org.j(), org.k());
            }
        }
    }

    private void buildGeometry(Particle particle, VertexConsumer vertexConsumer, Camera camera, float partialTicks)
    {
        particle.render(vertexConsumer, camera, partialTicks);
    }
}

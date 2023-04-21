package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;

@Mixin(ViewArea.class)
public abstract class MixinViewFrustum {
    @Redirect(
            method = "repositionCamera",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;setOrigin(III)V"
            )
    )
    private void replayModReplay_updatePositionAndMarkForUpdate(
            RenderChunk renderChunk,
            int x, int y, int z
    ) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!pos.equals(renderChunk.getOrigin())) {
            renderChunk.setOrigin(x, y, z);
            renderChunk.setDirty(false);
        }
    }
}

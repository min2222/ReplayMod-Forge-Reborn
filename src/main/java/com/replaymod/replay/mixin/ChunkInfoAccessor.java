package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

@Mixin(targets = { "net.minecraft.client.renderer.LevelRenderer$RenderChunkInfo" })
public interface ChunkInfoAccessor {
	@Accessor("chunk")
	ChunkRenderDispatcher.RenderChunk getChunk();
}

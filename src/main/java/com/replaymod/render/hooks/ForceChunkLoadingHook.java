package com.replaymod.render.hooks;

import com.replaymod.render.utils.FlawlessFrames;

import net.minecraft.client.renderer.LevelRenderer;

public class ForceChunkLoadingHook {
	private final LevelRenderer hooked;

	public ForceChunkLoadingHook(LevelRenderer renderGlobal) {
		this.hooked = renderGlobal;
		FlawlessFrames.setEnabled(true);
		IForceChunkLoading.from(renderGlobal).replayModRender_setHook(this);
	}

	public void uninstall() {
		IForceChunkLoading.from(this.hooked).replayModRender_setHook((ForceChunkLoadingHook) null);
		FlawlessFrames.setEnabled(false);
	}

	public interface IBlockOnChunkRebuilds {
		boolean uploadEverythingBlocking();
	}
}

package com.replaymod.render.hooks;
import net.minecraft.client.renderer.LevelRenderer;

public class ForceChunkLoadingHook {

    private final LevelRenderer hooked;

    public ForceChunkLoadingHook(LevelRenderer renderGlobal) {
        this.hooked = renderGlobal;

        IForceChunkLoading.from(renderGlobal).replayModRender_setHook(this);
    }

    public void uninstall() {
        IForceChunkLoading.from(hooked).replayModRender_setHook(null);
    }

    public interface IBlockOnChunkRebuilds {
        boolean uploadEverythingBlocking();
    }
}

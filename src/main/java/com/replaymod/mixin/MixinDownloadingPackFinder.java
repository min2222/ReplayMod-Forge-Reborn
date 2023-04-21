package com.replaymod.mixin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.replaymod.gui.utils.Consumer;
import com.replaymod.recording.packet.ResourcePackRecorder;

import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackSource;

@Mixin(ClientPackSource.class)
public abstract class MixinDownloadingPackFinder implements ResourcePackRecorder.IDownloadingPackFinder {
    private Consumer<File> requestCallback;

    @Override
    public void setRequestCallback(Consumer<File> callback) {
        requestCallback = callback;
    }

    @Inject(method = "setServerPack", at = @At("HEAD"))
    private void recordDownloadedPack(
            File file,
            PackSource arg,
            CallbackInfoReturnable<CompletableFuture<Void>> ci
    ) {
        if (requestCallback != null) {
            requestCallback.consume(file);
            requestCallback = null;
        }
    }
}

package com.replaymod.replay.mixin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.recording.packet.ResourcePackRecorder;

import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackSource;

@Mixin({ ClientPackSource.class })
public abstract class MixinDownloadingPackFinder implements ResourcePackRecorder.IDownloadingPackFinder {
	private Consumer<File> requestCallback;

	public void setRequestCallback(Consumer<File> callback) {
		this.requestCallback = callback;
	}

	@Inject(method = {
			"setServerPack(Ljava/io/File;Lnet/minecraft/server/packs/repository/PackSource;)Ljava/util/concurrent/CompletableFuture;" }, at = {
					@At("HEAD") })
	private void recordDownloadedPack(File file, PackSource arg, CallbackInfoReturnable<CompletableFuture<Void>> ci) {
		if (this.requestCallback != null) {
			this.requestCallback.consume(file);
			this.requestCallback = null;
		}

	}
}

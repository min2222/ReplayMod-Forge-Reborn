package com.replaymod.replay.mixin;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.network.Connection;

@Mixin({ Minecraft.class })
public interface MinecraftAccessor {
	@Accessor("timer")
	Timer getTimer();

	@Accessor("timer")
	@Mutable
	void setTimer(Timer renderTickCounter);

	@Accessor("pendingReload")
	CompletableFuture<Void> getResourceReloadFuture();

	@Accessor("pendingReload")
	void setResourceReloadFuture(CompletableFuture<Void> completableFuture);

	@Accessor("progressTasks")
	Queue<Runnable> getRenderTaskQueue();

	@Accessor("delayedCrash")
	Supplier<CrashReport> getCrashReporter();

	@Accessor("pendingConnection")
	void setConnection(Connection clientConnection);
}

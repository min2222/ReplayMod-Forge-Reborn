package com.replaymod.mixin;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;


@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor
    Timer getTimer();

    @Accessor
    @Mutable
    void setTimer(Timer value);

    @Accessor("pendingReload")
    CompletableFuture<Void> getResourceReloadFuture();

    @Accessor("pendingReload")
    void setResourceReloadFuture(CompletableFuture<Void> value);

    @Accessor("progressTasks")
    Queue<Runnable> getRenderTaskQueue();

    @Accessor("delayedCrash")
    Supplier<CrashReport> getCrashReporter();
}

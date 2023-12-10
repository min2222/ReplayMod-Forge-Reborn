package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.events.PostRenderCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.versions.MCVer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

@Mixin({ Minecraft.class })
public abstract class MixinMinecraftCore extends ReentrantBlockableEventLoop<Runnable>
		implements MCVer.MinecraftMethodAccessor {
	public MixinMinecraftCore(String string_1) {
		super(string_1);
	}

	@Shadow
	protected abstract void handleKeybinds();

	public void replayModProcessKeyBinds() {
		this.handleKeybinds();
	}

	public void replayModExecuteTaskQueue() {
		this.runAllTasks();
	}

	@Inject(method = { "runTick" }, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V") })
	private void preRender(boolean unused, CallbackInfo ci) {
		((PreRenderCallback) PreRenderCallback.EVENT.invoker()).preRender();
	}

	@Inject(method = { "runTick" }, at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", shift = Shift.AFTER) })
	private void postRender(boolean unused, CallbackInfo ci) {
		((PostRenderCallback) PostRenderCallback.EVENT.invoker()).postRender();
	}
}

package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.MinecraftMethodAccessor;
import com.replaymod.core.events.PostRenderCallback;
import com.replaymod.core.events.PreRenderCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft
        extends ReentrantBlockableEventLoop<Runnable>
        implements MinecraftMethodAccessor {

    public MixinMinecraft(String string_1) {
        super(string_1);
    }

    @Shadow
    protected abstract void handleKeybinds();

    public void replayModProcessKeyBinds() {
    	handleKeybinds();
    }

    public void replayModExecuteTaskQueue() {
    	runAllTasks();
    }

    @Inject(method = "runTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"))
    private void preRender(boolean unused, CallbackInfo ci) {
        PreRenderCallback.EVENT.invoker().preRender();
    }

    @Inject(method = "runTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V",
                    shift = At.Shift.AFTER))
    private void postRender(boolean unused, CallbackInfo ci) {
        PostRenderCallback.EVENT.invoker().postRender();
    }
}

package com.replaymod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Collections2;
import com.replaymod.gui.versions.callbacks.InitScreenCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public class MixinScreen {
    @Shadow
    protected @Final
    List<Widget> renderables;

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void preInit(Minecraft minecraftClient_1, int int_1, int int_2, CallbackInfo ci) {
        InitScreenCallback.Pre.EVENT.invoker().preInitScreen((Screen) (Object) this);
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("RETURN"))
    private void init(Minecraft minecraftClient_1, int int_1, int int_2, CallbackInfo ci) {
        InitScreenCallback.EVENT.invoker().initScreen((Screen) (Object)this, Collections2.transform(Collections2.filter(this.renderables, it -> it instanceof AbstractWidget), it -> (AbstractWidget)it));
    }
}

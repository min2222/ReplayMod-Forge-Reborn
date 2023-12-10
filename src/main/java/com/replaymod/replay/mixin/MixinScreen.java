package com.replaymod.replay.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Collections2;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

@Mixin(value = { Screen.class }, priority = 1100)
public class MixinScreen {
	@Shadow
	@Final
	private List<GuiEventListener> children;

	@Inject(method = { "init(Lnet/minecraft/client/Minecraft;II)V" }, at = { @At("HEAD") })
	private void preInit(CallbackInfo ci) {
		this.firePreInit();
	}

	@Inject(method = { "init(Lnet/minecraft/client/Minecraft;II)V" }, at = { @At("TAIL") })
	private void init(CallbackInfo ci) {
		this.firePostInit();
	}

	@Unique
	private void firePreInit() {
		((InitScreenCallback.Pre) InitScreenCallback.Pre.EVENT.invoker()).preInitScreen(Screen.class.cast(this));
	}

	@Unique
	private void firePostInit() {
		((InitScreenCallback) InitScreenCallback.EVENT.invoker()).initScreen(Screen.class.cast(this),
				Collections2.transform(Collections2.filter(this.children, (it) -> {
					return it instanceof AbstractWidget;
				}), (it) -> {
					return (AbstractWidget) it;
				}));
	}
}

package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.Window;
import com.replaymod.render.gui.progress.VirtualWindow;
import com.replaymod.render.hooks.MinecraftClientExt;

import net.minecraft.client.Minecraft;

@Mixin({ Minecraft.class })
public class Mixin_SuppressFramebufferResizeDuringRender implements MinecraftClientExt {
	@Unique
	private VirtualWindow windowDelegate;

	public void setWindowDelegate(VirtualWindow window) {
		this.windowDelegate = window;
	}

	@Inject(method = { "resizeDisplay" }, at = { @At("HEAD") }, cancellable = true)
	private void suppressResizeDuringRender(CallbackInfo ci) {
		VirtualWindow delegate = this.windowDelegate;
		if (delegate != null && delegate.isBound()) {
			Window window = Minecraft.class.cast(this).getWindow();
			delegate.onResolutionChanged(window.getWidth(), window.getHeight());
			ci.cancel();
		}

	}
}

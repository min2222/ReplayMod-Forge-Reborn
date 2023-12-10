package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.gui.components.spectator.SpectatorGui;

@Mixin({ SpectatorGui.class })
public abstract class MixinGuiSpectator {
	@Inject(method = { "onMouseScrolled" }, at = { @At("HEAD") }, cancellable = true)
	public void isInReplay(int i, CallbackInfo ci) {
		if (MCVer.getMinecraft().player instanceof CameraEntity) {
			ci.cancel();
		}

	}
}

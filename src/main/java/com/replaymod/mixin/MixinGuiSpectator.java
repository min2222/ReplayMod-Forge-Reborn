package com.replaymod.mixin;

import static com.replaymod.core.versions.MCVer.getMinecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.gui.components.spectator.SpectatorGui;

@Mixin(SpectatorGui.class)
public abstract class MixinGuiSpectator {
    @Inject(method = "onMouseScrolled", at = @At("HEAD"), cancellable = true)
    public void isInReplay(
            int p_205381_, CallbackInfo ci
    ) {
        // Prevent spectator gui from opening while in a replay
        if (getMinecraft().player instanceof CameraEntity) {
            ci.cancel();
        }
    }
}

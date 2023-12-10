package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;

@Pseudo
@Mixin(targets = { "net.optifine.shaders.Shaders" }, remap = false)
public abstract class Mixin_ShowSpectatedHand_OF {
	@Inject(method = { "setRenderingFirstPersonHand" }, at = { @At("HEAD") }, remap = false)
	private static void fakePlayerGameMode(boolean renderingHand, CallbackInfo ci) {
		LocalPlayer camera = MCVer.getMinecraft().player;
		if (camera instanceof CameraEntity) {
			MultiPlayerGameMode interactionManager = MCVer.getMinecraft().gameMode;

			assert interactionManager != null;

			if (renderingHand) {
				interactionManager.setLocalMode(camera.isSpectator() ? GameType.SPECTATOR : GameType.SURVIVAL);
			} else {
				interactionManager.setLocalMode(GameType.SPECTATOR);
			}
		}

	}
}

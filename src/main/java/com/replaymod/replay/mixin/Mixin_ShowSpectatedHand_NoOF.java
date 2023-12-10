package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.GameType;

@Mixin({ GameRenderer.class })
public abstract class Mixin_ShowSpectatedHand_NoOF {
	@Redirect(method = {
			"renderItemInHand" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"))
	private GameType getGameMode(MultiPlayerGameMode interactionManager) {
		LocalPlayer camera = MCVer.getMinecraft().player;
		if (camera instanceof CameraEntity) {
			return camera.isSpectator() ? GameType.SPECTATOR : GameType.SURVIVAL;
		} else {
			return interactionManager.getPlayerMode();
		}
	}
}

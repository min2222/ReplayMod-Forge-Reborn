package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;

@Mixin({ MultiPlayerGameMode.class })
public abstract class MixinPlayerControllerMP {
	@Shadow
	private Minecraft minecraft;
	@Shadow
	private ClientPacketListener connection;

	@Inject(method = {
			"createPlayer(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/stats/StatsCounter;Lnet/minecraft/client/ClientRecipeBook;ZZ)Lnet/minecraft/client/player/LocalPlayer;" }, at = {
					@At("HEAD") }, cancellable = true)
	private void replayModReplay_createReplayCamera(ClientLevel worldIn, StatsCounter statisticsManager,
			ClientRecipeBook recipeBookClient, boolean lastIsHoldingSneakKey, boolean lastSprinting,
			CallbackInfoReturnable<LocalPlayer> ci) {
		if (ReplayModReplay.instance.getReplayHandler() != null) {
			ci.setReturnValue(
					new CameraEntity(this.minecraft, worldIn, this.connection, statisticsManager, recipeBookClient));
			ci.cancel();
		}
	}

	@Inject(method = { "isAlwaysFlying" }, at = { @At("HEAD") }, cancellable = true)
	private void replayModReplay_isSpectator(CallbackInfoReturnable<Boolean> ci) {
		if (this.minecraft.player instanceof CameraEntity) {
			ci.setReturnValue(this.minecraft.player.isSpectator());
		}
	}
}

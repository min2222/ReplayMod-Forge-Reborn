package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.replaymod.extras.ReplayModExtras;
import com.replaymod.extras.playeroverview.PlayerOverview;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(value = { EntityRenderer.class }, priority = 1200)
public abstract class MixinRender {
	@Inject(method = { "shouldRender" }, at = { @At("HEAD") }, cancellable = true)
	public void replayModExtras_isPlayerHidden(Entity entity, @Coerce Object camera, double camX, double camY,
			double camZ, CallbackInfoReturnable<Boolean> ci) {
		ReplayModExtras.instance.get(PlayerOverview.class).ifPresent((playerOverview) -> {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				if (playerOverview.isHidden(player.getUUID())) {
					ci.setReturnValue(false);
				}
			}

		});
	}
}

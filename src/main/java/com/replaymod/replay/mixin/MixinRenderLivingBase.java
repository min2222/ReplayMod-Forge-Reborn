package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin({ LivingEntityRenderer.class })
public abstract class MixinRenderLivingBase {
	@Inject(method = { "shouldShowName" }, at = { @At("HEAD") }, cancellable = true)
	private void replayModReplay_canRenderInvisibleName(LivingEntity entity, CallbackInfoReturnable<Boolean> ci) {
		Player thePlayer = MCVer.getMinecraft().player;
		if (thePlayer instanceof CameraEntity && entity.isInvisible()) {
			ci.setReturnValue(false);
		}

	}

	@Redirect(method = {
			"render" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z"))
	private boolean replayModReplay_shouldInvisibleNotBeRendered(LivingEntity entity, Player thePlayer) {
		return thePlayer instanceof CameraEntity || entity.isInvisibleTo(thePlayer);
	}
}

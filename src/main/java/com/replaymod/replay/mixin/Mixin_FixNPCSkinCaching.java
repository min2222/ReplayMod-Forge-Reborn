package com.replaymod.replay.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;

@Mixin({ AbstractClientPlayer.class })
public abstract class Mixin_FixNPCSkinCaching {
	@Shadow
	@Nullable
	protected abstract PlayerInfo getPlayerInfo();

	@Inject(method = { "<init>" }, at = { @At("RETURN") })
	private void forceCachePlayerListEntry(CallbackInfo ci) {
		if (AbstractClientPlayer.class.cast(this) instanceof RemotePlayer) {
			if (Minecraft.getInstance().getConnection() != null) {
				try {
					this.getPlayerInfo();
				} catch (Exception var3) {
					var3.printStackTrace();
				}

			}
		}
	}
}

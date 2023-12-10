package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.replaymod.core.ReplayMod;
import com.replaymod.replay.Setting;
import com.replaymod.replay.handler.GuiHandler;

import net.minecraft.client.gui.screens.TitleScreen;

@Mixin({ TitleScreen.class })
public abstract class Mixin_MoveRealmsButton {
	private static final String REALMS_INIT = "Lnet/minecraft/client/realms/gui/screen/RealmsNotificationsScreen;init(Lnet/minecraft/client/Minecraft;II)V";

	@ModifyArg(method = {
			"init" }, at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(Lnet/minecraft/client/Minecraft;II)V"), index = 2)
	private int adjustRealmsButton(int height) {
		String setting = (String) ReplayMod.instance.getSettingsRegistry().get(Setting.MAIN_MENU_BUTTON);
		if (GuiHandler.MainMenuButtonPosition.valueOf(setting) == GuiHandler.MainMenuButtonPosition.BIG) {
			height -= 96;
		}

		return height;
	}
}

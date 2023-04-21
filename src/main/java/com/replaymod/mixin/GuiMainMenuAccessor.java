package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.screens.TitleScreen;

@Mixin(TitleScreen.class)
public interface GuiMainMenuAccessor {
	//TODO
    /*@Accessor("realmsNotificationsScreen")
    Screen getRealmsNotification();

    @Accessor("realmsNotificationsScreen")
    void setRealmsNotification(Screen value);*/
}

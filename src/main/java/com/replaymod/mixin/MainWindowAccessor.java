package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.platform.Window;

@Mixin(Window.class)
public interface MainWindowAccessor {
    @Accessor("framebufferWidth")
    int getFramebufferWidth();

    @Accessor("framebufferWidth")
    void setFramebufferWidth(int value);

    @Accessor("framebufferHeight")
    int getFramebufferHeight();

    @Accessor("framebufferHeight")
    void setFramebufferHeight(int value);
}

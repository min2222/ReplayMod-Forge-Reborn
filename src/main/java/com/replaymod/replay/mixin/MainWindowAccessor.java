package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.platform.Window;

@Mixin({ Window.class })
public interface MainWindowAccessor {
	@Accessor("framebufferWidth")
	int getFramebufferWidth();

	@Accessor("framebufferWidth")
	void setFramebufferWidth(int i);

	@Accessor("framebufferHeight")
	int getFramebufferHeight();

	@Accessor("framebufferHeight")
	void setFramebufferHeight(int i);

	@Invoker("onFramebufferResize")
	void invokeOnFramebufferSizeChanged(long l, int i, int j);
}

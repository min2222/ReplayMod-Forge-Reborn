package com.replaymod.replay.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.KeyMapping;

@Mixin({ KeyMapping.class })
public interface KeyBindingAccessor {
	@Accessor("clickCount")
	int getPressTime();

	@Accessor("clickCount")
	void setPressTime(int i);
}

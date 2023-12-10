package com.replaymod.core;

import org.spongepowered.asm.mixin.Mixins;

public class ReplayModMMLauncher implements Runnable {
	private static boolean ran;

	public void run() {
		if (!ran) {
			ran = true;
			Mixins.addConfiguration("mixins.compat.mapwriter.replaymod.json");
			Mixins.addConfiguration("mixins.compat.shaders.replaymod.json");
			Mixins.addConfiguration("mixins.core.replaymod.json");
			Mixins.addConfiguration("mixins.extras.playeroverview.replaymod.json");
			Mixins.addConfiguration("mixins.recording.replaymod.json");
			Mixins.addConfiguration("mixins.render.blend.replaymod.json");
			Mixins.addConfiguration("mixins.render.replaymod.json");
			Mixins.addConfiguration("mixins.replay.replaymod.json");
		}
	}
}

package com.replaymod.compat.optifine;

import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.render.events.ReplayRenderCallback;

import net.minecraft.client.Minecraft;

public class DisableFastRender extends EventRegistrations {
	private final Minecraft mc = Minecraft.getInstance();
	private boolean wasFastRender = false;

	public DisableFastRender() {
		this.on(ReplayRenderCallback.Pre.EVENT, (renderer) -> {
			this.onRenderBegin();
		});
		this.on(ReplayRenderCallback.Post.EVENT, (renderer) -> {
			this.onRenderEnd();
		});
	}

	private void onRenderBegin() {
		if (MCVer.hasOptifine()) {
			try {
				this.wasFastRender = (Boolean) OptifineReflection.gameSettings_ofFastRender.get(this.mc.options);
				OptifineReflection.gameSettings_ofFastRender.set(this.mc.options, false);
			} catch (IllegalAccessException var2) {
				var2.printStackTrace();
			}

		}
	}

	private void onRenderEnd() {
		if (MCVer.hasOptifine()) {
			try {
				OptifineReflection.gameSettings_ofFastRender.set(this.mc.options, this.wasFastRender);
			} catch (IllegalAccessException var2) {
				var2.printStackTrace();
			}

		}
	}
}

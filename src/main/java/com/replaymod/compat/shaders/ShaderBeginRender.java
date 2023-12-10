package com.replaymod.compat.shaders;

import java.lang.reflect.InvocationTargetException;

import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.Minecraft;

public class ShaderBeginRender extends EventRegistrations {
	private final Minecraft mc = Minecraft.getInstance();

	public ShaderBeginRender() {
		this.on(PreRenderCallback.EVENT, this::onRenderTickStart);
	}

	private void onRenderTickStart() {
		if (ShaderReflection.shaders_beginRender != null) {
			if (ShaderReflection.config_isShaders != null) {
				try {
					if (((EntityRendererHandler.IEntityRenderer) this.mc.gameRenderer)
							.replayModRender_getHandler() == null) {
						return;
					}

					if (!(Boolean) ShaderReflection.config_isShaders.invoke((Object) null)) {
						return;
					}

					ShaderReflection.shaders_beginRender.invoke((Object) null, this.mc,
							this.mc.gameRenderer.getMainCamera(), this.mc.getPartialTick(), 0);
				} catch (InvocationTargetException | IllegalAccessException var2) {
					var2.printStackTrace();
				}

			}
		}
	}
}

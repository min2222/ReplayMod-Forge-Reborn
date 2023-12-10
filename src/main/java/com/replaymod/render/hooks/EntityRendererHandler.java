package com.replaymod.render.hooks;

import java.io.IOException;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.events.PostRenderCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.events.PreRenderHandCallback;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.Setting;
import com.replaymod.render.capturer.CaptureData;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.capturer.WorldRenderer;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.mixin.GameRendererAccessor;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class EntityRendererHandler extends EventRegistrations implements WorldRenderer {
	public final Minecraft mc = MCVer.getMinecraft();
	protected final RenderSettings settings;
	private final RenderInfo renderInfo;
	public CaptureData data;
	public boolean omnidirectional;
	private final long startTime;

	public EntityRendererHandler(RenderSettings settings, RenderInfo renderInfo) {
		this.settings = settings;
		this.renderInfo = renderInfo;
		this.startTime = Util.getNanos();
		this.on(PreRenderHandCallback.EVENT, () -> {
			return this.omnidirectional;
		});
		((EntityRendererHandler.IEntityRenderer) this.mc.gameRenderer).replayModRender_setHandler(this);
		this.register();
	}

	public void renderWorld(float partialTicks, CaptureData data) {
		this.data = data;
		long offsetMillis;
		if ((Boolean) ReplayMod.instance.getSettingsRegistry().get(Setting.FRAME_TIME_FROM_WORLD_TIME)) {
			offsetMillis = (long) ReplayModReplay.instance.getReplayHandler().getReplaySender().currentTimeStamp();
		} else {
			offsetMillis = (long) this.renderInfo.getFramesDone() * 1000L / (long) this.settings.getFramesPerSecond();
		}

		long frameStartTimeNano = this.startTime + offsetMillis * 1000000L;
		this.renderWorld(partialTicks, frameStartTimeNano);
	}

	public void renderWorld(float partialTicks, long finishTimeNano) {
		((PreRenderCallback) PreRenderCallback.EVENT.invoker()).preRender();
		if (this.mc.level != null && this.mc.player != null) {
			GameRendererAccessor gameRenderer = (GameRendererAccessor) this.mc.gameRenderer;
			Screen orgScreen = this.mc.screen;
			boolean orgPauseOnLostFocus = this.mc.options.pauseOnLostFocus;
			boolean orgRenderHand = gameRenderer.getRenderHand();

			try {
				this.mc.screen = null;
				this.mc.options.pauseOnLostFocus = false;
				if (this.omnidirectional) {
					gameRenderer.setRenderHand(false);
				}

				this.mc.gameRenderer.render(partialTicks, finishTimeNano, true);
			} finally {
				this.mc.screen = orgScreen;
				this.mc.options.pauseOnLostFocus = orgPauseOnLostFocus;
				gameRenderer.setRenderHand(orgRenderHand);
			}
		}

		((PostRenderCallback) PostRenderCallback.EVENT.invoker()).postRender();
	}

	public void close() throws IOException {
		((EntityRendererHandler.IEntityRenderer) this.mc.gameRenderer)
				.replayModRender_setHandler((EntityRendererHandler) null);
		this.unregister();
	}

	public void setOmnidirectional(boolean omnidirectional) {
		this.omnidirectional = omnidirectional;
	}

	public RenderSettings getSettings() {
		return this.settings;
	}

	public RenderInfo getRenderInfo() {
		return this.renderInfo;
	}

	public interface IEntityRenderer {
		void replayModRender_setHandler(EntityRendererHandler entityRendererHandler);

		EntityRendererHandler replayModRender_getHandler();
	}
}

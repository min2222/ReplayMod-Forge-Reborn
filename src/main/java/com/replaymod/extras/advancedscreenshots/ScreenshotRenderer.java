package com.replaymod.extras.advancedscreenshots;

import com.mojang.blaze3d.platform.Window;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.blend.BlendState;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.hooks.ForceChunkLoadingHook;
import com.replaymod.render.rendering.Pipelines;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;

public class ScreenshotRenderer implements RenderInfo {
	private final Minecraft mc = MCVer.getMinecraft();
	private final RenderSettings settings;
	private int framesDone;

	public ScreenshotRenderer(RenderSettings settings) {
		this.settings = settings;
	}

	public boolean renderScreenshot() throws Throwable {
		try {
			Window window = this.mc.getWindow();
			int widthBefore = window.getWidth();
			int heightBefore = window.getHeight();
			ForceChunkLoadingHook clrg = new ForceChunkLoadingHook(this.mc.levelRenderer);
			if (this.settings.getRenderMethod() == RenderSettings.RenderMethod.BLEND) {
				BlendState.setState(new BlendState(this.settings.getOutputFile()));
				Pipelines.newBlendPipeline(this).run();
			} else {
				Pipelines.newPipeline(this.settings.getRenderMethod(), this,
						new ScreenshotWriter(this.settings.getOutputFile())).run();
			}

			clrg.uninstall();
			MCVer.resizeMainWindow(this.mc, widthBefore, heightBefore);
			return true;
		} catch (OutOfMemoryError var5) {
			var5.printStackTrace();
			CrashReport report = CrashReport.forThrowable(var5, "Creating Equirectangular Screenshot");
			MCVer.getMinecraft().delayCrashRaw(report);
			return false;
		}
	}

	public ReadableDimension getFrameSize() {
		return new Dimension(this.settings.getVideoWidth(), this.settings.getVideoHeight());
	}

	public int getFramesDone() {
		return this.framesDone;
	}

	public int getTotalFrames() {
		return 2;
	}

	public float updateForNextFrame() {
		++this.framesDone;
		return this.mc.getPartialTick();
	}

	public RenderSettings getRenderSettings() {
		return this.settings;
	}
}

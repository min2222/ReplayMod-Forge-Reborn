package com.replaymod.render.blend;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.render.capturer.CaptureData;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.capturer.WorldRenderer;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.rendering.Channel;
import com.replaymod.render.rendering.FrameCapturer;
import com.replaymod.render.utils.ByteBufferPool;

import net.minecraft.client.Minecraft;

public class BlendFrameCapturer implements FrameCapturer<BitmapFrame> {
	protected final WorldRenderer worldRenderer;
	protected final RenderInfo renderInfo;
	protected int framesDone;

	public BlendFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo) {
		this.worldRenderer = worldRenderer;
		this.renderInfo = renderInfo;
	}

	public boolean isDone() {
		return this.framesDone >= this.renderInfo.getTotalFrames();
	}

	public Map<Channel, BitmapFrame> process() {
		if (this.framesDone == 0) {
			BlendState.getState().setup();
		}

		this.renderInfo.updateForNextFrame();
		BlendState.getState().preFrame(this.framesDone);
		this.worldRenderer.renderWorld(Minecraft.getInstance().getPartialTick(), (CaptureData) null);
		BlendState.getState().postFrame(this.framesDone);
		BitmapFrame frame = new BitmapFrame(this.framesDone++, new Dimension(0, 0), 0, ByteBufferPool.allocate(0));
		return Collections.singletonMap(Channel.BRGA, frame);
	}

	public void close() throws IOException {
		BlendState.getState().tearDown();
		BlendState.setState((BlendState) null);
	}
}

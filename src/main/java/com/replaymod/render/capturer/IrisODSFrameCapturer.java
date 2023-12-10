package com.replaymod.render.capturer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.frame.CubicOpenGlFrame;
import com.replaymod.render.frame.ODSOpenGlFrame;
import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.rendering.Channel;
import com.replaymod.render.rendering.FrameCapturer;

import net.coderbot.iris.Iris;
import net.coderbot.iris.config.IrisConfig;

public class IrisODSFrameCapturer implements FrameCapturer<ODSOpenGlFrame> {
	public static final String SHADER_PACK_NAME = "assets/replaymod/iris/ods";
	public static IrisODSFrameCapturer INSTANCE;
	private final CubicPboOpenGlFrameCapturer left;
	private final CubicPboOpenGlFrameCapturer right;
	private final String prevShaderPack;
	private final boolean prevShadersEnabled;
	private int direction;
	private boolean isLeftEye;

	public IrisODSFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo, int frameSize) {
		RenderInfo fakeInfo = new RenderInfo() {
			private int call;
			private float partialTicks;

			public ReadableDimension getFrameSize() {
				return renderInfo.getFrameSize();
			}

			public int getFramesDone() {
				return renderInfo.getFramesDone();
			}

			public int getTotalFrames() {
				return renderInfo.getTotalFrames();
			}

			public float updateForNextFrame() {
				if (this.call++ % 2 == 0) {
					this.partialTicks = renderInfo.updateForNextFrame();
				}

				return this.partialTicks;
			}

			public RenderSettings getRenderSettings() {
				return renderInfo.getRenderSettings();
			}
		};
		this.left = new IrisODSFrameCapturer.CubicStereoFrameCapturer(worldRenderer, fakeInfo, frameSize);
		this.right = new IrisODSFrameCapturer.CubicStereoFrameCapturer(worldRenderer, fakeInfo, frameSize);
		INSTANCE = this;
		IrisConfig irisConfig = Iris.getIrisConfig();
		this.prevShaderPack = (String) irisConfig.getShaderPackName().orElse(null);
		this.prevShadersEnabled = irisConfig.areShadersEnabled();
		setShaderPack("assets/replaymod/iris/ods", true);
	}

	private static void setShaderPack(String name, boolean enabled) {
		IrisConfig irisConfig = Iris.getIrisConfig();
		irisConfig.setShaderPackName(name);
		irisConfig.setShadersEnabled(enabled);

		try {
			irisConfig.save();
			Iris.reload();
		} catch (IOException var4) {
			throw new RuntimeException(var4);
		}
	}

	public int getDirection() {
		return this.direction;
	}

	public boolean isLeftEye() {
		return this.isLeftEye;
	}

	public boolean isDone() {
		return this.left.isDone() && this.right.isDone();
	}

	public Map<Channel, ODSOpenGlFrame> process() {
		this.isLeftEye = true;
		Map<Channel, CubicOpenGlFrame> leftChannels = this.left.process();
		this.isLeftEye = false;
		Map<Channel, CubicOpenGlFrame> rightChannels = this.right.process();
		if (leftChannels != null && rightChannels != null) {
			Map<Channel, ODSOpenGlFrame> result = new HashMap();
			Channel[] var4 = Channel.values();
			int var5 = var4.length;

			for (int var6 = 0; var6 < var5; ++var6) {
				Channel channel = var4[var6];
				CubicOpenGlFrame leftFrame = (CubicOpenGlFrame) leftChannels.get(channel);
				CubicOpenGlFrame rightFrame = (CubicOpenGlFrame) rightChannels.get(channel);
				if (leftFrame != null && rightFrame != null) {
					result.put(channel, new ODSOpenGlFrame(leftFrame, rightFrame));
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public void close() throws IOException {
		this.left.close();
		this.right.close();
		INSTANCE = null;
		setShaderPack(this.prevShaderPack, this.prevShadersEnabled);
	}

	private class CubicStereoFrameCapturer extends CubicPboOpenGlFrameCapturer {
		public CubicStereoFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo, int frameSize) {
			super(worldRenderer, renderInfo, frameSize);
		}

		protected OpenGlFrame renderFrame(int frameId, float partialTicks, CubicOpenGlFrameCapturer.Data captureData) {
			IrisODSFrameCapturer.this.direction = captureData.ordinal();
			return super.renderFrame(frameId, partialTicks, captureData);
		}
	}
}

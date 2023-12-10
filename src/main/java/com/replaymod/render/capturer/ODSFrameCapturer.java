package com.replaymod.render.capturer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.frame.CubicOpenGlFrame;
import com.replaymod.render.frame.ODSOpenGlFrame;
import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.hooks.FogStateCallback;
import com.replaymod.render.hooks.Texture2DStateCallback;
import com.replaymod.render.rendering.Channel;
import com.replaymod.render.rendering.FrameCapturer;
import com.replaymod.render.shader.Program;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;

public class ODSFrameCapturer implements FrameCapturer<ODSOpenGlFrame> {
	private static final ResourceLocation vertexResource = new ResourceLocation("replaymod", "shader/ods.vert");
	private static final ResourceLocation fragmentResource = new ResourceLocation("replaymod", "shader/ods.frag");
	private final CubicPboOpenGlFrameCapturer left;
	private final CubicPboOpenGlFrameCapturer right;
	private final Program shaderProgram;
	private final Program.Uniform directionVariable;
	private final Program.Uniform leftEyeVariable;
	private EventRegistrations renderStateEvents;

	public ODSFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo, int frameSize) {
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
					ODSFrameCapturer.this.unbindProgram();
					this.partialTicks = renderInfo.updateForNextFrame();
					ODSFrameCapturer.this.bindProgram();
				}

				return this.partialTicks;
			}

			public RenderSettings getRenderSettings() {
				return renderInfo.getRenderSettings();
			}
		};
		this.left = new ODSFrameCapturer.CubicStereoFrameCapturer(worldRenderer, fakeInfo, frameSize);
		this.right = new ODSFrameCapturer.CubicStereoFrameCapturer(worldRenderer, fakeInfo, frameSize);

		try {
			this.shaderProgram = new Program(vertexResource, fragmentResource);
			this.leftEyeVariable = this.shaderProgram.getUniformVariable("leftEye");
			this.directionVariable = this.shaderProgram.getUniformVariable("direction");
		} catch (Exception var6) {
			throw new ReportedException(CrashReport.forThrowable(var6, "Creating ODS shaders"));
		}
	}

	private void bindProgram() {
		this.shaderProgram.use();
		this.setTexture("texture", 0);
		this.setTexture("overlay", 1);
		this.setTexture("lightMap", 2);
		this.renderStateEvents = new EventRegistrations();
		Program.Uniform[] texture2DUniforms = new Program.Uniform[] {
				this.shaderProgram.getUniformVariable("textureEnabled"),
				this.shaderProgram.getUniformVariable("overlayEnabled"),
				this.shaderProgram.getUniformVariable("lightMapEnabled") };
		this.renderStateEvents.on(Texture2DStateCallback.EVENT, (id, enabled) -> {
			if (id >= 0 && id < texture2DUniforms.length) {
				texture2DUniforms[id].set(enabled);
			}

		});
		Program.Uniform fogUniform = this.shaderProgram.getUniformVariable("fogEnabled");
		EventRegistrations var10000 = this.renderStateEvents;
		Objects.requireNonNull(fogUniform);
		var10000.on(FogStateCallback.EVENT, fogUniform::set);
		this.renderStateEvents.register();
	}

	private void unbindProgram() {
		this.renderStateEvents.unregister();
		this.renderStateEvents = null;
		this.shaderProgram.stopUsing();
	}

	private void setTexture(String texture, int i) {
		this.shaderProgram.getUniformVariable(texture).set(i);
	}

	public boolean isDone() {
		return this.left.isDone() && this.right.isDone();
	}

	public Map<Channel, ODSOpenGlFrame> process() {
		this.bindProgram();
		this.leftEyeVariable.set(true);
		Map<Channel, CubicOpenGlFrame> leftChannels = this.left.process();
		this.leftEyeVariable.set(false);
		Map<Channel, CubicOpenGlFrame> rightChannels = this.right.process();
		this.unbindProgram();
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
		this.shaderProgram.delete();
	}

	private class CubicStereoFrameCapturer extends CubicPboOpenGlFrameCapturer {
		public CubicStereoFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo, int frameSize) {
			super(worldRenderer, renderInfo, frameSize);
		}

		protected OpenGlFrame renderFrame(int frameId, float partialTicks, CubicOpenGlFrameCapturer.Data captureData) {
			ODSFrameCapturer.this.directionVariable.set(captureData.ordinal());
			return super.renderFrame(frameId, partialTicks, captureData);
		}
	}
}

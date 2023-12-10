package com.replaymod.render.rendering;

import java.util.Map;

import com.replaymod.render.RenderSettings;
import com.replaymod.render.blend.BlendFrameCapturer;
import com.replaymod.render.capturer.CubicOpenGlFrameCapturer;
import com.replaymod.render.capturer.CubicPboOpenGlFrameCapturer;
import com.replaymod.render.capturer.IrisODSFrameCapturer;
import com.replaymod.render.capturer.ODSFrameCapturer;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.capturer.SimpleOpenGlFrameCapturer;
import com.replaymod.render.capturer.SimplePboOpenGlFrameCapturer;
import com.replaymod.render.capturer.StereoscopicOpenGlFrameCapturer;
import com.replaymod.render.capturer.StereoscopicPboOpenGlFrameCapturer;
import com.replaymod.render.capturer.WorldRenderer;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.frame.CubicOpenGlFrame;
import com.replaymod.render.frame.ODSOpenGlFrame;
import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.frame.StereoscopicOpenGlFrame;
import com.replaymod.render.hooks.EntityRendererHandler;
import com.replaymod.render.processor.CubicToBitmapProcessor;
import com.replaymod.render.processor.DummyProcessor;
import com.replaymod.render.processor.EquirectangularToBitmapProcessor;
import com.replaymod.render.processor.ODSToBitmapProcessor;
import com.replaymod.render.processor.OpenGlToBitmapProcessor;
import com.replaymod.render.processor.StereoscopicToBitmapProcessor;
import com.replaymod.render.utils.PixelBufferObject;

import net.minecraftforge.fml.ModList;

public class Pipelines {
	public static Pipeline newPipeline(RenderSettings.RenderMethod method, RenderInfo renderInfo,
			FrameConsumer<BitmapFrame> consumer) {
		switch (method) {
		case DEFAULT:
			return newDefaultPipeline(renderInfo, consumer);
		case STEREOSCOPIC:
			return newStereoscopicPipeline(renderInfo, consumer);
		case CUBIC:
			return newCubicPipeline(renderInfo, consumer);
		case EQUIRECTANGULAR:
			return newEquirectangularPipeline(renderInfo, consumer);
		case ODS:
			return newODSPipeline(renderInfo, consumer);
		case BLEND:
			throw new UnsupportedOperationException("Use newBlendPipeline instead!");
		default:
			throw new UnsupportedOperationException("Unknown method: " + method);
		}
	}

	public static Pipeline<OpenGlFrame, BitmapFrame> newDefaultPipeline(RenderInfo renderInfo,
			FrameConsumer<BitmapFrame> consumer) {
		RenderSettings settings = renderInfo.getRenderSettings();
		WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
		Object capturer;
		if (!PixelBufferObject.SUPPORTED && !settings.isDepthMap()) {
			capturer = new SimpleOpenGlFrameCapturer(worldRenderer, renderInfo);
		} else {
			capturer = new SimplePboOpenGlFrameCapturer(worldRenderer, renderInfo);
		}

		return new Pipeline(worldRenderer, (FrameCapturer) capturer, new OpenGlToBitmapProcessor(), consumer);
	}

	public static Pipeline<StereoscopicOpenGlFrame, BitmapFrame> newStereoscopicPipeline(RenderInfo renderInfo,
			FrameConsumer<BitmapFrame> consumer) {
		RenderSettings settings = renderInfo.getRenderSettings();
		WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
		Object capturer;
		if (!PixelBufferObject.SUPPORTED && !settings.isDepthMap()) {
			capturer = new StereoscopicOpenGlFrameCapturer(worldRenderer, renderInfo);
		} else {
			capturer = new StereoscopicPboOpenGlFrameCapturer(worldRenderer, renderInfo);
		}

		return new Pipeline(worldRenderer, (FrameCapturer) capturer, new StereoscopicToBitmapProcessor(), consumer);
	}

	public static Pipeline<CubicOpenGlFrame, BitmapFrame> newCubicPipeline(RenderInfo renderInfo,
			FrameConsumer<BitmapFrame> consumer) {
		RenderSettings settings = renderInfo.getRenderSettings();
		WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
		Object capturer;
		if (!PixelBufferObject.SUPPORTED && !settings.isDepthMap()) {
			capturer = new CubicOpenGlFrameCapturer(worldRenderer, renderInfo, settings.getVideoWidth() / 4);
		} else {
			capturer = new CubicPboOpenGlFrameCapturer(worldRenderer, renderInfo, settings.getVideoWidth() / 4);
		}

		return new Pipeline(worldRenderer, (FrameCapturer) capturer, new CubicToBitmapProcessor(), consumer);
	}

	public static Pipeline<CubicOpenGlFrame, BitmapFrame> newEquirectangularPipeline(RenderInfo renderInfo,
			FrameConsumer<BitmapFrame> consumer) {
		RenderSettings settings = renderInfo.getRenderSettings();
		WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
		EquirectangularToBitmapProcessor processor = new EquirectangularToBitmapProcessor(settings.getVideoWidth(),
				settings.getVideoHeight(), settings.getSphericalFovX());
		Object capturer;
		if (!PixelBufferObject.SUPPORTED && !settings.isDepthMap()) {
			capturer = new CubicOpenGlFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize());
		} else {
			capturer = new CubicPboOpenGlFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize());
		}

		return new Pipeline(worldRenderer, (FrameCapturer) capturer, processor, consumer);
	}

	public static Pipeline<ODSOpenGlFrame, BitmapFrame> newODSPipeline(RenderInfo renderInfo,
			FrameConsumer<BitmapFrame> consumer) {
		RenderSettings settings = renderInfo.getRenderSettings();
		WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
		ODSToBitmapProcessor processor = new ODSToBitmapProcessor(settings.getVideoWidth(), settings.getVideoHeight(),
				settings.getSphericalFovX());
		boolean iris = ModList.get().isLoaded("iris");
		FrameCapturer<ODSOpenGlFrame> capturer = iris
				? new IrisODSFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize())
				: new ODSFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize());
		return new Pipeline(worldRenderer, (FrameCapturer) capturer, processor, consumer);
	}

	public static Pipeline<BitmapFrame, BitmapFrame> newBlendPipeline(RenderInfo renderInfo) {
		RenderSettings settings = renderInfo.getRenderSettings();
		WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
		FrameCapturer<BitmapFrame> capturer = new BlendFrameCapturer(worldRenderer, renderInfo);
		FrameConsumer<BitmapFrame> consumer = new FrameConsumer<BitmapFrame>() {
			public void consume(Map<Channel, BitmapFrame> channels) {
			}

			public void close() {
			}

			public boolean isParallelCapable() {
				return true;
			}
		};
		return new Pipeline(worldRenderer, capturer, new DummyProcessor(), consumer);
	}
}

package com.replaymod.render;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyexr.EXRChannelInfo;
import org.lwjgl.util.tinyexr.EXRChannelInfo.Buffer;
import org.lwjgl.util.tinyexr.EXRHeader;
import org.lwjgl.util.tinyexr.EXRImage;
import org.lwjgl.util.tinyexr.TinyEXR;

import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.rendering.Channel;
import com.replaymod.render.rendering.FrameConsumer;
import com.replaymod.render.utils.ByteBufferPool;
import com.replaymod.render.utils.Lwjgl3Loader;

import net.minecraft.CrashReport;

public class EXRWriter implements FrameConsumer<BitmapFrame> {
	private static final int COMPRESSION = Runtime.getRuntime().availableProcessors() >= 8 ? 2 : 0;
	private final Path outputFolder;
	private final boolean keepAlpha;

	public static FrameConsumer<BitmapFrame> create(Path outputFolder, boolean keepAlpha) {
		return Lwjgl3Loader.createFrameConsumer(EXRWriter.class, new Class[] { Path.class, Boolean.TYPE },
				new Object[] { outputFolder, keepAlpha });
	}

	public EXRWriter(Path outputFolder, boolean keepAlpha) throws IOException {
		this.outputFolder = outputFolder;
		this.keepAlpha = keepAlpha;
		Files.createDirectories(outputFolder);
	}

	public void consume(Map<Channel, BitmapFrame> channels) {
		BitmapFrame bgraFrame = (BitmapFrame) channels.get(Channel.BRGA);
		BitmapFrame depthFrame = (BitmapFrame) channels.get(Channel.DEPTH);
		Path var10000 = this.outputFolder;
		int var10001 = bgraFrame.getFrameId();
		Path path = var10000.resolve(var10001 + ".exr");
		ReadableDimension size = bgraFrame.getSize();
		ByteBuffer bgra = bgraFrame.getByteBuffer();
		int width = size.getWidth();
		int height = size.getHeight();
		int numChannels = 4 + (depthFrame != null ? 1 : 0);
		MemoryStack.stackPush();
		EXRHeader header = EXRHeader.mallocStack();
		TinyEXR.InitEXRHeader(header);
		Buffer channelInfos = EXRChannelInfo.mallocStack(numChannels);
		IntBuffer pixelTypes = MemoryStack.stackMallocInt(numChannels);
		IntBuffer requestedPixelTypes = MemoryStack.stackMallocInt(numChannels);
		EXRImage image = EXRImage.mallocStack();
		TinyEXR.InitEXRImage(image);
		PointerBuffer imagePointers = MemoryStack.stackMallocPointer(numChannels);
		FloatBuffer images = MemoryUtil.memAllocFloat(width * height * numChannels);
		PointerBuffer err = MemoryStack.stackMallocPointer(1);

		try {
			header.num_channels(numChannels);
			header.channels(channelInfos);
			header.pixel_types(pixelTypes);
			header.requested_pixel_types(requestedPixelTypes);
			header.compression_type(COMPRESSION);
			MemoryUtil.memASCII("A", true, ((EXRChannelInfo) channelInfos.get(0)).name());
			MemoryUtil.memASCII("B", true, ((EXRChannelInfo) channelInfos.get(1)).name());
			MemoryUtil.memASCII("G", true, ((EXRChannelInfo) channelInfos.get(2)).name());
			MemoryUtil.memASCII("R", true, ((EXRChannelInfo) channelInfos.get(3)).name());

			for (int i = 0; i < numChannels; ++i) {
				pixelTypes.put(i, 2);
				requestedPixelTypes.put(i, 1);
			}

			if (depthFrame != null) {
				MemoryUtil.memASCII("Z", true, ((EXRChannelInfo) channelInfos.get(4)).name());
				requestedPixelTypes.put(4, 2);
			}

			image.num_channels(numChannels);
			image.width(width);
			image.height(height);
			image.images(imagePointers);
			FloatBuffer[] bgrChannels = new FloatBuffer[4];
			FloatBuffer depthChannel = null;

			int alphaMask;
			for (alphaMask = 0; alphaMask < numChannels; ++alphaMask) {
				FloatBuffer channel = images.slice();
				channel.position(width * height * alphaMask);
				imagePointers.put(alphaMask, channel.slice());
				if (alphaMask == 4) {
					depthChannel = channel;
				} else {
					bgrChannels[(alphaMask + 3) % 4] = channel;
				}
			}

			alphaMask = this.keepAlpha ? 0 : 255;

			int ret;
			for (ret = 0; ret < height; ++ret) {
				for (int x = 0; x < width; ++x) {
					bgrChannels[0].put((float) (bgra.get() & 255) / 255.0F);
					bgrChannels[1].put((float) (bgra.get() & 255) / 255.0F);
					bgrChannels[2].put((float) (bgra.get() & 255) / 255.0F);
					bgrChannels[3].put((float) (bgra.get() & 255 | alphaMask) / 255.0F);
				}
			}

			if (depthFrame != null && depthChannel != null) {
				depthChannel.put(depthFrame.getByteBuffer().asFloatBuffer());
			}

			ret = TinyEXR.SaveEXRImageToFile(image, header, path.toString(), err);
			if (ret != 0) {
				String message = err.getStringASCII(0);
				TinyEXR.FreeEXRErrorMessage(err.getByteBuffer(0));
				throw new IOException(message);
			}
		} catch (Throwable var26) {
			MCVer.getMinecraft().delayCrashRaw(CrashReport.forThrowable(var26, "Exporting EXR frame"));
		} finally {
			MemoryUtil.memFree(images);
			MemoryStack.stackPop();
			channels.values().forEach((it) -> {
				ByteBufferPool.release(it.getByteBuffer());
			});
		}

	}

	public void close() {
	}

	public boolean isParallelCapable() {
		return true;
	}
}

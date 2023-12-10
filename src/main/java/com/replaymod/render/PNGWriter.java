package com.replaymod.render;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import com.replaymod.render.blend.Util;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.rendering.Channel;
import com.replaymod.render.rendering.FrameConsumer;
import com.replaymod.render.utils.ByteBufferPool;

import net.minecraft.CrashReport;

public class PNGWriter implements FrameConsumer<BitmapFrame> {
	private final Path outputFolder;
	private final boolean keepAlpha;

	public PNGWriter(Path outputFolder, boolean keepAlpha) throws IOException {
		this.outputFolder = outputFolder;
		this.keepAlpha = keepAlpha;
		Files.createDirectories(outputFolder);
	}

	public void consume(Map<Channel, BitmapFrame> channels) {
		BitmapFrame bgraFrame = (BitmapFrame) channels.get(Channel.BRGA);
		BitmapFrame depthFrame = (BitmapFrame) channels.get(Channel.DEPTH);

		try {
			if (bgraFrame != null) {
				this.withImage(bgraFrame, (image) -> {
					image.writePNG(this.outputFolder.resolve(bgraFrame.getFrameId() + ".png").toFile());
				});
			}

			if (depthFrame != null) {
				this.withImage(depthFrame, (image) -> {
					image.writePNG(this.outputFolder.resolve(depthFrame.getFrameId() + ".depth.png").toFile());
				});
			}
		} catch (Throwable var8) {
			MCVer.getMinecraft().delayCrashRaw(CrashReport.forThrowable(var8, "Exporting EXR frame"));
		} finally {
			channels.values().forEach((it) -> {
				ByteBufferPool.release(it.getByteBuffer());
			});
		}

	}

	private void withImage(BitmapFrame frame, Util.IOConsumer<Image> consumer) throws IOException {
		byte alphaMask = (byte) (this.keepAlpha ? 0 : 255);
		ByteBuffer buffer = frame.getByteBuffer();
		ReadableDimension size = frame.getSize();
		int width = size.getWidth();
		int height = size.getHeight();
		Image image = new Image(width, height);

		try {
			int y = 0;

			while (true) {
				if (y >= height) {
					consumer.accept(image);
					break;
				}

				for (int x = 0; x < width; ++x) {
					byte b = buffer.get();
					byte g = buffer.get();
					byte r = buffer.get();
					byte a = buffer.get();
					image.setRGBA(x, y, r, g, b, a | alphaMask);
				}

				++y;
			}
		} catch (Throwable var16) {
			try {
				image.close();
			} catch (Throwable var15) {
				var16.addSuppressed(var15);
			}

			throw var16;
		}

		image.close();
	}

	public void close() {
	}

	public boolean isParallelCapable() {
		return true;
	}
}

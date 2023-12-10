package com.replaymod.render.processor;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.Validate;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.frame.CubicOpenGlFrame;
import com.replaymod.render.utils.ByteBufferPool;

public class EquirectangularToBitmapProcessor extends AbstractFrameProcessor<CubicOpenGlFrame, BitmapFrame> {
	private static final byte IMAGE_BACK = 0;
	private static final byte IMAGE_FRONT = 1;
	private static final byte IMAGE_LEFT = 2;
	private static final byte IMAGE_RIGHT = 3;
	private static final byte IMAGE_TOP = 4;
	private static final byte IMAGE_BOTTOM = 5;
	private final int frameSize;
	private final int width;
	private final int height;
	private final byte[][] image;
	private final int[][] imageX;
	private final int[][] imageY;

	public EquirectangularToBitmapProcessor(int outputWidth, int outputHeight, int sphericalFovX) {
		this.width = outputWidth;
		this.height = outputHeight;
		int fullWidth;
		if (sphericalFovX < 360) {
			fullWidth = Math.round((float) (this.width * 360) / (float) sphericalFovX);
		} else {
			fullWidth = this.width;
		}

		int fullHeight = fullWidth / 2;
		this.frameSize = fullWidth / 4;
		this.image = new byte[this.height][this.width];
		this.imageX = new int[this.height][this.width];
		this.imageY = new int[this.height][this.width];
		int xOffset = (fullWidth - this.width) / 2;
		int yOffset = (fullHeight - this.height) / 2;

		for (int x = 0; x < this.width; ++x) {
			int i = xOffset + x;
			double yaw = 6.283185307179586D * (double) i / (double) fullWidth;
			int piQuarter = 8 * i / fullWidth - 4;
			byte target;
			if (piQuarter < -3) {
				target = 0;
			} else if (piQuarter < -1) {
				target = 2;
			} else if (piQuarter < 1) {
				target = 1;
			} else if (piQuarter < 3) {
				target = 3;
			} else {
				target = 0;
			}

			double fYaw = (yaw + 0.7853981633974483D) % 1.5707963267948966D - 0.7853981633974483D;
			double d = 1.0D / Math.cos(fYaw);
			double gcXN = (Math.tan(fYaw) + 1.0D) / 2.0D;

			for (int y = 0; y < this.height; ++y) {
				int j = yOffset + y;
				double cXN = gcXN;
				byte pt = target;
				double pitch = 3.141592653589793D * (double) j / (double) fullHeight - 1.5707963267948966D;
				double cYN = (Math.tan(pitch) * d + 1.0D) / 2.0D;
				double pd;
				if (cYN >= 1.0D) {
					pd = Math.tan(1.5707963267948966D - pitch);
					cXN = (-Math.sin(yaw) * pd + 1.0D) / 2.0D;
					cYN = (Math.cos(yaw) * pd + 1.0D) / 2.0D;
					pt = 5;
				}

				if (cYN < 0.0D) {
					pd = Math.tan(1.5707963267948966D - pitch);
					cXN = (Math.sin(yaw) * pd + 1.0D) / 2.0D;
					cYN = (Math.cos(yaw) * pd + 1.0D) / 2.0D;
					pt = 4;
				}

				int imgX = (int) Math.min((double) (this.frameSize - 1), cXN * (double) this.frameSize);
				int imgY = (int) Math.min((double) (this.frameSize - 1), cYN * (double) this.frameSize);
				this.image[y][x] = pt;
				this.imageX[y][x] = imgX;
				this.imageY[y][x] = this.frameSize - imgY - 1;
			}
		}

	}

	public BitmapFrame process(CubicOpenGlFrame rawFrame) {
		Validate.isTrue(rawFrame.getLeft().getSize().getWidth() == this.frameSize, "Frame size must be %d but was %d",
				this.frameSize, rawFrame.getLeft().getSize().getWidth());
		int bpp = rawFrame.getLeft().getBytesPerPixel();
		ByteBuffer result = ByteBufferPool.allocate(this.width * this.height * bpp);
		ByteBuffer[] images = new ByteBuffer[] { rawFrame.getBack().getByteBuffer(),
				rawFrame.getFront().getByteBuffer(), rawFrame.getLeft().getByteBuffer(),
				rawFrame.getRight().getByteBuffer(), rawFrame.getTop().getByteBuffer(),
				rawFrame.getBottom().getByteBuffer() };
		byte[] pixel = new byte[bpp];

		for (int y = 0; y < this.height; ++y) {
			byte[] image = this.image[y];
			int[] imageX = this.imageX[y];
			int[] imageY = this.imageY[y];

			for (int x = 0; x < this.width; ++x) {
				ByteBuffer source = images[image[x]];
				source.position((imageX[x] + imageY[x] * this.frameSize) * bpp);
				source.get(pixel);
				result.put(pixel);
			}
		}

		result.rewind();
		ByteBufferPool.release(rawFrame.getLeft().getByteBuffer());
		ByteBufferPool.release(rawFrame.getRight().getByteBuffer());
		ByteBufferPool.release(rawFrame.getFront().getByteBuffer());
		ByteBufferPool.release(rawFrame.getBack().getByteBuffer());
		ByteBufferPool.release(rawFrame.getTop().getByteBuffer());
		ByteBufferPool.release(rawFrame.getBottom().getByteBuffer());
		return new BitmapFrame(rawFrame.getFrameId(), new Dimension(this.width, this.height), bpp, result);
	}

	public int getFrameSize() {
		return this.frameSize;
	}
}

package com.replaymod.lib.de.johni0702.minecraft.gui.versions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;

import net.minecraft.client.renderer.texture.DynamicTexture;

public class Image implements AutoCloseable {
	private NativeImage inner;

	public Image(int width, int height) {
		this(new NativeImage(Format.RGBA, width, height, true));
	}

	public Image(NativeImage inner) {
		this.inner = inner;
	}

	public NativeImage getInner() {
		return this.inner;
	}

	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	public void close() {
		if (this.inner != null) {
			this.inner.close();
			this.inner = null;
		}

	}

	public int getWidth() {
		return this.inner.getWidth();
	}

	public int getHeight() {
		return this.inner.getHeight();
	}

	public void setRGBA(int x, int y, int r, int g, int b, int a) {
		this.inner.setPixelRGBA(x, y, (a & 255) << 24 | (b & 255) << 16 | (g & 255) << 8 | r & 255);
	}

	public static Image read(Path path) throws IOException {
		return read(Files.newInputStream(path));
	}

	public static Image read(InputStream in) throws IOException {
		return new Image(NativeImage.read(in));
	}

	public void writePNG(File file) throws IOException {
		this.inner.writeToFile(file);
	}

	public void writePNG(OutputStream outputStream) throws IOException {
		Path tmp = Files.createTempFile("tmp", ".png");

		try {
			this.inner.writeToFile(tmp);
			Files.copy(tmp, outputStream);
		} finally {
			Files.delete(tmp);
		}

	}

	public Image scaledSubRect(int x, int y, int width, int height, int scaledWidth, int scaledHeight) {
		NativeImage dst = new NativeImage(this.inner.format(), scaledWidth, scaledHeight, false);
		this.inner.resizeSubRectTo(x, y, width, height, dst);
		return new Image(dst);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public BufferedImage toBufferedImage() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			this.writePNG((OutputStream) out);
			return ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
		} catch (IOException var3) {
			throw new RuntimeException(var3);
		}
	}

	public DynamicTexture toTexture() {
		return new DynamicTexture(this.inner);
	}
}

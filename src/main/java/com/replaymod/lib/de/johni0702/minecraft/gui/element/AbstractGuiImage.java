package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.google.common.base.Preconditions;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGuiImage<T extends AbstractGuiImage<T>> extends AbstractGuiElement<T>
		implements IGuiImage<T> {
	private DynamicTexture texture;
	private ResourceLocation resourceLocation;
	private int u;
	private int v;
	private int uWidth;
	private int vHeight;
	private int textureWidth;
	private int textureHeight;
	private AbstractGuiImage<T> copyOf;

	public AbstractGuiImage() {
	}

	public AbstractGuiImage(GuiContainer container) {
		super(container);
	}

	public AbstractGuiImage(AbstractGuiImage<T> copyOf) {
		this.texture = copyOf.texture;
		this.resourceLocation = copyOf.resourceLocation;
		this.u = copyOf.u;
		this.v = copyOf.v;
		this.uWidth = copyOf.uWidth;
		this.vHeight = copyOf.vHeight;
		this.textureWidth = copyOf.textureWidth;
		this.textureHeight = copyOf.textureHeight;
		this.copyOf = copyOf;
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		if (this.texture != null) {
			renderer.bindTexture(this.texture.getId());
		} else {
			renderer.bindTexture(this.resourceLocation);
		}

		int w = size.getWidth();
		int h = size.getHeight();
		renderer.drawTexturedRect(0, 0, this.u, this.v, w, h, this.uWidth, this.vHeight, this.textureWidth,
				this.textureHeight);
	}

	protected void finalize() throws Throwable {
		super.finalize();
		if (this.texture != null && this.copyOf == null) {
			this.getMinecraft().execute(new AbstractGuiImage.Finalizer(this.texture));
		}

	}

	public ReadableDimension calcMinSize() {
		return new Dimension(0, 0);
	}

	public T setTexture(Image img) {
		Preconditions.checkState(this.copyOf == null, "Cannot change texture of copy.");
		this.resourceLocation = null;
		if (this.texture != null) {
			this.texture.releaseId();
		}

		this.texture = img.toTexture();
		this.textureWidth = this.uWidth = img.getWidth();
		this.textureHeight = this.vHeight = img.getHeight();
		return this.getThis();
	}

	public T setTexture(ResourceLocation resourceLocation) {
		Preconditions.checkState(this.copyOf == null, "Cannot change texture of copy.");
		if (this.texture != null) {
			this.texture.releaseId();
			this.texture = null;
		}

		this.resourceLocation = resourceLocation;
		this.textureWidth = this.textureHeight = 256;
		return this.getThis();
	}

	public T setTexture(ResourceLocation resourceLocation, int u, int v, int width, int height) {
		this.setTexture(resourceLocation);
		this.setUV(u, v);
		this.setUVSize(width, height);
		return this.getThis();
	}

	public T setU(int u) {
		this.u = u;
		return this.getThis();
	}

	public T setV(int v) {
		this.v = v;
		return this.getThis();
	}

	public T setUV(int u, int v) {
		this.setU(u);
		return this.setV(v);
	}

	public T setUWidth(int width) {
		this.uWidth = width;
		return this.getThis();
	}

	public T setVHeight(int height) {
		this.vHeight = height;
		return this.getThis();
	}

	public T setUVSize(int width, int height) {
		this.setUWidth(width);
		return this.setVHeight(height);
	}

	private static final class Finalizer implements Runnable {
		private final DynamicTexture texture;

		public Finalizer(DynamicTexture texture) {
			this.texture = texture;
		}

		public void run() {
			this.texture.releaseId();
		}
	}
}

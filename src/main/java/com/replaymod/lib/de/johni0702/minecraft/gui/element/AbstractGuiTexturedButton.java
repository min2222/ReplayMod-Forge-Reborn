package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public abstract class AbstractGuiTexturedButton<T extends AbstractGuiTexturedButton<T>> extends AbstractGuiClickable<T>
		implements Clickable, IGuiTexturedButton<T> {
	private ResourceLocation texture;
	private SoundEvent sound;
	private ReadableDimension textureSize;
	private ReadableDimension textureTotalSize;
	private ReadablePoint textureNormal;
	private ReadablePoint textureHover;
	private ReadablePoint textureDisabled;

	public AbstractGuiTexturedButton() {
		this.sound = SoundEvents.UI_BUTTON_CLICK;
		this.textureSize = new ReadableDimension() {
			public int getWidth() {
				return AbstractGuiTexturedButton.this.getMaxSize().getWidth();
			}

			public int getHeight() {
				return AbstractGuiTexturedButton.this.getMaxSize().getHeight();
			}

			public void getSize(WritableDimension dest) {
				AbstractGuiTexturedButton.this.getMaxSize().getSize(dest);
			}
		};
	}

	public AbstractGuiTexturedButton(GuiContainer container) {
		super(container);
		this.sound = SoundEvents.UI_BUTTON_CLICK;
		this.textureSize = new ReadableDimension() {
			public int getWidth() {
				return AbstractGuiTexturedButton.this.getMaxSize().getWidth();
			}

			public int getHeight() {
				return AbstractGuiTexturedButton.this.getMaxSize().getHeight();
			}

			public void getSize(WritableDimension dest) {
				AbstractGuiTexturedButton.this.getMaxSize().getSize(dest);
			}
		};
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		renderer.bindTexture(this.texture);
		ReadablePoint texture = this.textureNormal;
		if (!this.isEnabled()) {
			texture = this.textureDisabled;
		} else if (this.isMouseHovering(new Point(renderInfo.mouseX, renderInfo.mouseY))) {
			texture = this.textureHover;
		}

		if (texture == null) {
			texture = this.textureNormal;
		}

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.blendFunc(770, 771);
		renderer.drawTexturedRect(0, 0, texture.getX(), texture.getY(), size.getWidth(), size.getHeight(),
				this.textureSize.getWidth(), this.textureSize.getHeight(), this.textureTotalSize.getWidth(),
				this.textureTotalSize.getHeight());
	}

	public ReadableDimension calcMinSize() {
		return new Dimension(0, 0);
	}

	public void onClick() {
		AbstractGuiButton.playClickSound(this.getMinecraft(), this.sound);
		super.onClick();
	}

	public T setTexture(ResourceLocation resourceLocation, int size) {
		return this.setTexture(resourceLocation, size, size);
	}

	public T setTexture(ResourceLocation resourceLocation, int width, int height) {
		this.texture = resourceLocation;
		this.textureTotalSize = new Dimension(width, height);
		return this.getThis();
	}

	public T setTextureSize(int size) {
		return this.setTextureSize(size, size);
	}

	public T setTextureSize(int width, int height) {
		this.textureSize = new Dimension(width, height);
		return this.getThis();
	}

	public T setTexturePosH(int x, int y) {
		return this.setTexturePosH(new Point(x, y));
	}

	public T setTexturePosV(int x, int y) {
		return this.setTexturePosV(new Point(x, y));
	}

	public T setTexturePosH(ReadablePoint pos) {
		this.textureNormal = pos;
		this.textureHover = new ReadablePoint() {
			public int getX() {
				return pos.getX() + AbstractGuiTexturedButton.this.textureSize.getWidth();
			}

			public int getY() {
				return pos.getY();
			}

			public void getLocation(WritablePoint dest) {
				dest.setLocation(this.getX(), this.getY());
			}
		};
		return this.getThis();
	}

	public T setTexturePosV(ReadablePoint pos) {
		this.textureNormal = pos;
		this.textureHover = new ReadablePoint() {
			public int getX() {
				return pos.getX();
			}

			public int getY() {
				return pos.getY() + AbstractGuiTexturedButton.this.textureSize.getHeight();
			}

			public void getLocation(WritablePoint dest) {
				dest.setLocation(this.getX(), this.getY());
			}
		};
		return this.getThis();
	}

	public T setTexturePos(int normalX, int normalY, int hoverX, int hoverY) {
		return this.setTexturePos(new Point(normalX, normalY), new Point(hoverX, hoverY));
	}

	public T setTexturePos(ReadablePoint normal, ReadablePoint hover) {
		this.textureNormal = normal;
		this.textureHover = hover;
		return this.getThis();
	}

	public T setTexturePos(int normalX, int normalY, int hoverX, int hoverY, int disabledX, int disabledY) {
		return this.setTexturePos(new Point(normalX, normalY), new Point(hoverX, hoverY),
				new Point(disabledX, disabledY));
	}

	public T setTexturePos(ReadablePoint normal, ReadablePoint hover, ReadablePoint disabled) {
		this.textureDisabled = disabled;
		return this.setTexturePos(normal, hover);
	}

	public T setSound(SoundEvent sound) {
		this.sound = sound;
		return this.getThis();
	}

	public SoundEvent getSound() {
		return this.sound;
	}

	public ResourceLocation getTexture() {
		return this.texture;
	}

	public ReadableDimension getTextureSize() {
		return this.textureSize;
	}

	public ReadableDimension getTextureTotalSize() {
		return this.textureTotalSize;
	}

	public ReadablePoint getTextureNormal() {
		return this.textureNormal;
	}

	public ReadablePoint getTextureHover() {
		return this.textureHover;
	}

	public ReadablePoint getTextureDisabled() {
		return this.textureDisabled;
	}
}

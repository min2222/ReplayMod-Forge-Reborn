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
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public abstract class AbstractGuiButton<T extends AbstractGuiButton<T>> extends AbstractGuiClickable<T>
		implements Clickable, IGuiButton<T> {
	protected static final ResourceLocation BUTTON_SOUND = new ResourceLocation("gui.button.press");
	protected static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("textures/gui/widgets.png");
	private SoundEvent sound;
	private int labelColor;
	private String label;
	private ResourceLocation texture;
	private ReadableDimension textureSize;
	private ReadablePoint spriteUV;
	private ReadableDimension spriteSize;

	public AbstractGuiButton() {
		this.sound = SoundEvents.UI_BUTTON_CLICK;
		this.labelColor = 14737632;
	}

	public AbstractGuiButton(GuiContainer container) {
		super(container);
		this.sound = SoundEvents.UI_BUTTON_CLICK;
		this.labelColor = 14737632;
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		byte texture = 1;
		int color = this.labelColor;
		if (!this.isEnabled()) {
			texture = 0;
			color = 10526880;
		} else if (this.isMouseHovering(new Point(renderInfo.mouseX, renderInfo.mouseY))) {
			texture = 2;
			color = 16777120;
		}

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(770, 771, 1, 0);
		RenderSystem.blendFunc(770, 771);
		int textureY = 46 + texture * 20;
		int halfWidth = size.getWidth() / 2;
		int secondHalfWidth = size.getWidth() - halfWidth;
		int halfHeight = size.getHeight() / 2;
		int secondHalfHeight = size.getHeight() - halfHeight;
		renderer.bindTexture(WIDGETS_TEXTURE);
		renderer.drawTexturedRect(0, 0, 0, textureY, halfWidth, halfHeight);
		renderer.drawTexturedRect(0, halfHeight, 0, textureY + 20 - secondHalfHeight, halfWidth, secondHalfHeight);
		renderer.drawTexturedRect(halfWidth, 0, 200 - secondHalfWidth, textureY, secondHalfWidth, halfHeight);
		renderer.drawTexturedRect(halfWidth, halfHeight, 200 - secondHalfWidth, textureY + 20 - secondHalfHeight,
				secondHalfWidth, secondHalfHeight);
		if (this.texture != null) {
			renderer.bindTexture(this.texture);
			if (this.spriteUV != null && this.textureSize != null) {
				ReadableDimension spriteSize = this.spriteSize != null ? this.spriteSize : this.getMinSize();
				renderer.drawTexturedRect(0, 0, this.spriteUV.getX(), this.spriteUV.getY(), size.getWidth(),
						size.getHeight(), spriteSize.getWidth(), spriteSize.getHeight(), this.textureSize.getWidth(),
						this.textureSize.getHeight());
			} else {
				renderer.drawTexturedRect(0, 0, 0, 0, size.getWidth(), size.getHeight());
			}
		}

		if (this.label != null) {
			renderer.drawCenteredString(halfWidth, (size.getHeight() - 8) / 2, color, this.label, true);
		}

	}

	public ReadableDimension calcMinSize() {
		if (this.label != null) {
			Font fontRenderer = MCVer.getFontRenderer();
			return new Dimension(fontRenderer.width(this.label), 20);
		} else {
			return new Dimension(0, 0);
		}
	}

	public void onClick() {
		playClickSound(this.getMinecraft());
		super.onClick();
	}

	public static void playClickSound(Minecraft mc) {
		playClickSound(mc, SoundEvents.UI_BUTTON_CLICK);
	}

	public static void playClickSound(Minecraft mc, SoundEvent sound) {
		mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
	}

	public T setLabel(String label) {
		this.label = label;
		return this.getThis();
	}

	public T setSound(SoundEvent sound) {
		this.sound = sound;
		return this.getThis();
	}

	public SoundEvent getSound() {
		return this.sound;
	}

	public T setI18nLabel(String label, Object... args) {
		return this.setLabel(I18n.get(label, args));
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabelColor(int labelColor) {
		this.labelColor = labelColor;
	}

	public ResourceLocation getTexture() {
		return this.texture;
	}

	public T setTexture(ResourceLocation texture) {
		this.texture = texture;
		return this.getThis();
	}

	public ReadableDimension getTextureSize() {
		return this.textureSize;
	}

	public T setTextureSize(ReadableDimension textureSize) {
		this.textureSize = textureSize;
		return this.getThis();
	}

	public ReadablePoint getSpriteUV() {
		return this.spriteUV;
	}

	public T setSpriteUV(ReadablePoint spriteUV) {
		this.spriteUV = spriteUV;
		return this.getThis();
	}

	public ReadableDimension getSpriteSize() {
		return this.spriteSize;
	}

	public T setSpriteSize(ReadableDimension spriteSize) {
		this.spriteSize = spriteSize;
		return this.getThis();
	}
}

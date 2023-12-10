package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface IGuiButton<T extends IGuiButton<T>> extends IGuiClickable<T> {
	T setLabel(String string);

	T setI18nLabel(String string, Object... objects);

	T setSound(SoundEvent soundEvent);

	String getLabel();

	ResourceLocation getTexture();

	T setTexture(ResourceLocation identifier);

	ReadableDimension getTextureSize();

	T setTextureSize(ReadableDimension readableDimension);

	default T setTextureSize(int width, int height) {
		return this.setTextureSize(new Dimension(width, height));
	}

	default T setTextureSize(int size) {
		return this.setTextureSize(size, size);
	}

	default T setTexture(ResourceLocation identifier, int width, int height) {
		return this.setTexture(identifier).setTextureSize(width, height);
	}

	default T setTexture(ResourceLocation resourceLocation, int size) {
		return this.setTexture(resourceLocation, size, size);
	}

	T setSpriteUV(ReadablePoint readablePoint);

	ReadablePoint getSpriteUV();

	default T setSpriteUV(int u, int v) {
		return this.setSpriteUV(new Point(u, v));
	}

	T setSpriteSize(ReadableDimension readableDimension);

	ReadableDimension getSpriteSize();

	default T setSpriteSize(int width, int height) {
		return this.setSpriteSize(new Dimension(width, height));
	}

	default T setSprite(int u, int v, int width, int height) {
		return this.setSpriteUV(u, v).setSpriteSize(width, height);
	}
}

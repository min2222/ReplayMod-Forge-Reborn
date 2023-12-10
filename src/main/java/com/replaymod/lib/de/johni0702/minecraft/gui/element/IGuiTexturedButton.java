package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface IGuiTexturedButton<T extends IGuiTexturedButton<T>> extends IGuiClickable<T> {
	ResourceLocation getTexture();

	ReadableDimension getTextureTotalSize();

	T setTexture(ResourceLocation identifier, int i);

	T setTexture(ResourceLocation identifier, int i, int j);

	ReadableDimension getTextureSize();

	T setTextureSize(int i);

	T setTextureSize(int i, int j);

	ReadablePoint getTextureNormal();

	ReadablePoint getTextureHover();

	ReadablePoint getTextureDisabled();

	T setTexturePosH(int i, int j);

	T setTexturePosV(int i, int j);

	T setTexturePosH(ReadablePoint readablePoint);

	T setTexturePosV(ReadablePoint readablePoint);

	T setTexturePos(int i, int j, int k, int l);

	T setTexturePos(ReadablePoint readablePoint, ReadablePoint readablePoint2);

	T setTexturePos(int i, int j, int k, int l, int m, int n);

	T setTexturePos(ReadablePoint readablePoint, ReadablePoint readablePoint2, ReadablePoint readablePoint3);

	T setSound(SoundEvent soundEvent);
}

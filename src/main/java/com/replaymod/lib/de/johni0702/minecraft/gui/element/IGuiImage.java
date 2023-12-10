package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;

import net.minecraft.resources.ResourceLocation;

public interface IGuiImage<T extends IGuiImage<T>> extends GuiElement<T> {
	T setTexture(Image image);

	T setTexture(ResourceLocation identifier);

	T setTexture(ResourceLocation identifier, int i, int j, int k, int l);

	T setU(int i);

	T setV(int i);

	T setUV(int i, int j);

	T setUWidth(int i);

	T setVHeight(int i);

	T setUVSize(int i, int j);
}

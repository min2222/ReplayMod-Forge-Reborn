package com.replaymod.lib.de.johni0702.minecraft.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

import net.minecraft.resources.ResourceLocation;

public interface GuiRenderer {
	ReadablePoint getOpenGlOffset();

	PoseStack getMatrixStack();

	ReadableDimension getSize();

	void setDrawingArea(int i, int j, int k, int l);

	void bindTexture(ResourceLocation identifier);

	void bindTexture(int i);

	void drawTexturedRect(int i, int j, int k, int l, int m, int n);

	void drawTexturedRect(int i, int j, int k, int l, int m, int n, int o, int p, int q, int r);

	void drawRect(int i, int j, int k, int l, int m);

	void drawRect(int i, int j, int k, int l, ReadableColor readableColor);

	void drawRect(int i, int j, int k, int l, int m, int n, int o, int p);

	void drawRect(int i, int j, int k, int l, ReadableColor readableColor, ReadableColor readableColor2,
			ReadableColor readableColor3, ReadableColor readableColor4);

	int drawString(int i, int j, int k, String string);

	int drawString(int i, int j, ReadableColor readableColor, String string);

	int drawCenteredString(int i, int j, int k, String string);

	int drawCenteredString(int i, int j, ReadableColor readableColor, String string);

	int drawString(int i, int j, int k, String string, boolean bl);

	int drawString(int i, int j, ReadableColor readableColor, String string, boolean bl);

	int drawCenteredString(int i, int j, int k, String string, boolean bl);

	int drawCenteredString(int i, int j, ReadableColor readableColor, String string, boolean bl);

	void invertColors(int i, int j, int k, int l);
}

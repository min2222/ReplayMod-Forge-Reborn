package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import com.mojang.blaze3d.platform.Window;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.Minecraft;

public class MouseUtils {
	private static final Minecraft mc = MCVer.getMinecraft();

	public static Point getMousePos() {
		int mouseX = (int) mc.mouseHandler.xpos();
		int mouseY = (int) mc.mouseHandler.ypos();
		Window mainWindow = MCVer.newScaledResolution(mc);
		mouseX = (int) Math
				.round((double) mouseX * (double) mainWindow.getGuiScaledWidth() / (double) mainWindow.getWidth());
		mouseY = (int) Math
				.round((double) mouseY * (double) mainWindow.getGuiScaledHeight() / (double) mainWindow.getHeight());
		return new Point(mouseX, mouseY);
	}

	public static Point getScaledDimensions() {
		Window res = MCVer.newScaledResolution(mc);
		return new Point(res.getGuiScaledWidth(), res.getGuiScaledHeight());
	}
}

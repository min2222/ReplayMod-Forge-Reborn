package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import java.util.Arrays;
import java.util.HashSet;

import com.google.common.base.Preconditions;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;

public class Utils {
	public static final int DOUBLE_CLICK_INTERVAL = 250;

	public static void link(Focusable... focusables) {
		Preconditions.checkArgument((new HashSet(Arrays.asList(focusables))).size() == focusables.length,
				"focusables must be unique and not null");

		for (int i = 0; i < focusables.length; ++i) {
			Focusable next = focusables[(i + 1) % focusables.length];
			focusables[i].setNext(next);
			next.setPrevious(focusables[i]);
		}

	}

	public static void drawDynamicRect(GuiRenderer renderer, int width, int height, int u, int v, int uWidth,
			int vHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder) {
		int textureBodyHeight = vHeight - topBorder - bottomBorder;
		int textureBodyWidth = uWidth - leftBorder - rightBorder;

		int x;
		int segmentWidth;
		int textureX;
		int y;
		int segmentHeight;
		for (x = 0; x < 2; ++x) {
			segmentWidth = x == 0 ? 0 : width - rightBorder;
			textureX = x == 0 ? u : u + uWidth - rightBorder;

			for (y = topBorder; y < height - bottomBorder; y += textureBodyHeight) {
				segmentHeight = Math.min(textureBodyHeight, height - bottomBorder - y);
				renderer.drawTexturedRect(segmentWidth, y, textureX, v + topBorder, leftBorder, segmentHeight);
			}

			renderer.drawTexturedRect(segmentWidth, 0, textureX, v, leftBorder, topBorder);
			renderer.drawTexturedRect(segmentWidth, height - bottomBorder, textureX, v + vHeight - bottomBorder,
					leftBorder, bottomBorder);
		}

		for (x = leftBorder; x < width - rightBorder; x += textureBodyWidth) {
			segmentWidth = Math.min(textureBodyWidth, width - rightBorder - x);
			textureX = u + leftBorder;

			for (y = topBorder; y < height - bottomBorder; y += textureBodyHeight) {
				segmentHeight = Math.min(textureBodyHeight, height - bottomBorder - y);
				renderer.drawTexturedRect(x, y, textureX, v + topBorder, segmentWidth, segmentHeight);
			}

			renderer.drawTexturedRect(x, 0, textureX, v, segmentWidth, topBorder);
			renderer.drawTexturedRect(x, height - bottomBorder, textureX, v + vHeight - bottomBorder, segmentWidth,
					bottomBorder);
		}

	}

	public static int clamp(int val, int min, int max) {
		return val < min ? min : (val > max ? max : val);
	}
}

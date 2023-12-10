package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import java.util.ArrayList;
import java.util.List;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.gui.Font;

public class StringUtils {
	public static String[] splitStringInMultipleRows(String string, int maxWidth) {
		if (string == null)
			return new String[0];
		Font fontRenderer = MCVer.getFontRenderer();
		List<String> rows = new ArrayList<>();
		String remaining = string;
		while (remaining.length() > 0) {
			String[] split = remaining.split(" ");
			String b = "";
			for (String sp : split) {
				b += sp + " ";
				if (fontRenderer.width(b.trim()) > maxWidth) {
					b = b.substring(0, b.trim().length() - (sp.length()));
					break;
				}
			}
			String trimmed = b.trim();
			rows.add(trimmed);
			try {
				remaining = remaining.substring(trimmed.length() + 1);
			} catch (Exception e) {
				break;
			}
		}

		return rows.toArray(new String[rows.size()]);
	}
}

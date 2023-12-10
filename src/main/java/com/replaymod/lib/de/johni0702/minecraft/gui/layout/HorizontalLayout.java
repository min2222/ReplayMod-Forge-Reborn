package com.replaymod.lib.de.johni0702.minecraft.gui.layout;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public class HorizontalLayout implements Layout {
	private static final HorizontalLayout.Data DEFAULT_DATA = new HorizontalLayout.Data(0.0D);
	private final HorizontalLayout.Alignment alignment;
	private int spacing;

	public HorizontalLayout() {
		this(HorizontalLayout.Alignment.LEFT);
	}

	public HorizontalLayout(HorizontalLayout.Alignment alignment) {
		this.alignment = alignment;
	}

	public Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> container,
			ReadableDimension size) {
		int x = 0;
		int spacing = 0;
		Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> map = new LinkedHashMap();

		Dimension elementSize;
		for (Iterator var6 = container.getElements().entrySet().iterator(); var6
				.hasNext(); x += elementSize.getWidth()) {
			Entry<GuiElement, LayoutData> entry = (Entry) var6.next();
			x += spacing;
			spacing = this.spacing;
			GuiElement element = (GuiElement) entry.getKey();
			HorizontalLayout.Data data = entry.getValue() instanceof HorizontalLayout.Data
					? (HorizontalLayout.Data) entry.getValue()
					: DEFAULT_DATA;
			elementSize = new Dimension(element.getMinSize());
			ReadableDimension elementMaxSize = element.getMaxSize();
			elementSize.setWidth(
					Math.min(size.getWidth() - x, Math.min(elementSize.getWidth(), elementMaxSize.getWidth())));
			elementSize.setHeight(Math.min(size.getHeight(), elementMaxSize.getHeight()));
			int remainingHeight = size.getHeight() - elementSize.getHeight();
			int y = (int) (data.alignment * (double) remainingHeight);
			map.put(element, Pair.of(new Point(x, y), elementSize));
		}

		if (this.alignment != HorizontalLayout.Alignment.LEFT) {
			int remaining = size.getWidth() - x;
			if (this.alignment == HorizontalLayout.Alignment.CENTER) {
				remaining /= 2;
			}

			Iterator var15 = map.values().iterator();

			while (var15.hasNext()) {
				Pair<ReadablePoint, ReadableDimension> pair = (Pair) var15.next();
				((Point) pair.getLeft()).translate(remaining, 0);
			}
		}

		return map;
	}

	public ReadableDimension calcMinSize(GuiContainer<?> container) {
		int maxHeight = 0;
		int width = 0;
		int spacing = 0;

		ReadableDimension minSize;
		for (Iterator var5 = container.getElements().entrySet().iterator(); var5
				.hasNext(); width += minSize.getWidth()) {
			Entry<GuiElement, LayoutData> entry = (Entry) var5.next();
			width += spacing;
			spacing = this.spacing;
			GuiElement element = (GuiElement) entry.getKey();
			minSize = element.getMinSize();
			int height = minSize.getHeight();
			if (height > maxHeight) {
				maxHeight = height;
			}
		}

		return new Dimension(width, maxHeight);
	}

	public int getSpacing() {
		return this.spacing;
	}

	public HorizontalLayout setSpacing(int spacing) {
		this.spacing = spacing;
		return this;
	}

	public static enum Alignment {
		LEFT, RIGHT, CENTER;

		// $FF: synthetic method
		private static HorizontalLayout.Alignment[] $values() {
			return new HorizontalLayout.Alignment[] { LEFT, RIGHT, CENTER };
		}
	}

	public static class Data implements LayoutData {
		private double alignment;

		public Data() {
			this(0.0D);
		}

		public Data(double alignment) {
			this.alignment = alignment;
		}

		public double getAlignment() {
			return this.alignment;
		}

		public void setAlignment(double alignment) {
			this.alignment = alignment;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o != null && this.getClass() == o.getClass()) {
				HorizontalLayout.Data data = (HorizontalLayout.Data) o;
				return Double.compare(data.alignment, this.alignment) == 0;
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[] { this.alignment });
		}

		public String toString() {
			return "Data{alignment=" + this.alignment + "}";
		}
	}
}

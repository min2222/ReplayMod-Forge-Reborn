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

public class VerticalLayout implements Layout {
	private static final VerticalLayout.Data DEFAULT_DATA = new VerticalLayout.Data(0.0D);
	private final VerticalLayout.Alignment alignment;
	private int spacing;

	public VerticalLayout() {
		this(VerticalLayout.Alignment.TOP);
	}

	public VerticalLayout(VerticalLayout.Alignment alignment) {
		this.alignment = alignment;
	}

	public Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> container,
			ReadableDimension size) {
		int y = 0;
		int spacing = 0;
		Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> map = new LinkedHashMap();

		Dimension elementSize;
		for (Iterator var6 = container.getElements().entrySet().iterator(); var6
				.hasNext(); y += elementSize.getHeight()) {
			Entry<GuiElement, LayoutData> entry = (Entry) var6.next();
			y += spacing;
			spacing = this.spacing;
			GuiElement element = (GuiElement) entry.getKey();
			VerticalLayout.Data data = entry.getValue() instanceof VerticalLayout.Data
					? (VerticalLayout.Data) entry.getValue()
					: DEFAULT_DATA;
			elementSize = new Dimension(element.getMinSize());
			ReadableDimension elementMaxSize = element.getMaxSize();
			elementSize.setHeight(
					Math.min(size.getHeight() - y, Math.min(elementSize.getHeight(), elementMaxSize.getHeight())));
			elementSize.setWidth(Math.min(size.getWidth(),
					((ReadableDimension) (data.maximizeWidth ? elementMaxSize : elementSize)).getWidth()));
			int remainingWidth = size.getWidth() - elementSize.getWidth();
			int x = (int) (data.alignment * (double) remainingWidth);
			map.put(element, Pair.of(new Point(x, y), elementSize));
		}

		if (this.alignment != VerticalLayout.Alignment.TOP) {
			int remaining = size.getHeight() - y;
			if (this.alignment == VerticalLayout.Alignment.CENTER) {
				remaining /= 2;
			}

			Iterator var15 = map.values().iterator();

			while (var15.hasNext()) {
				Pair<ReadablePoint, ReadableDimension> pair = (Pair) var15.next();
				((Point) pair.getLeft()).translate(0, remaining);
			}
		}

		return map;
	}

	public ReadableDimension calcMinSize(GuiContainer<?> container) {
		int maxWidth = 0;
		int height = 0;
		int spacing = 0;

		ReadableDimension minSize;
		for (Iterator var5 = container.getElements().entrySet().iterator(); var5
				.hasNext(); height += minSize.getHeight()) {
			Entry<GuiElement, LayoutData> entry = (Entry) var5.next();
			height += spacing;
			spacing = this.spacing;
			GuiElement element = (GuiElement) entry.getKey();
			minSize = element.getMinSize();
			int width = minSize.getWidth();
			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		return new Dimension(maxWidth, height);
	}

	public int getSpacing() {
		return this.spacing;
	}

	public VerticalLayout setSpacing(int spacing) {
		this.spacing = spacing;
		return this;
	}

	public static enum Alignment {
		TOP, BOTTOM, CENTER;

		// $FF: synthetic method
		private static VerticalLayout.Alignment[] $values() {
			return new VerticalLayout.Alignment[] { TOP, BOTTOM, CENTER };
		}
	}

	public static class Data implements LayoutData {
		private double alignment;
		private boolean maximizeWidth;

		public Data() {
			this(0.0D);
		}

		public Data(double alignment) {
			this(alignment, true);
		}

		public Data(double alignment, boolean maximizeWidth) {
			this.alignment = alignment;
			this.maximizeWidth = maximizeWidth;
		}

		public double getAlignment() {
			return this.alignment;
		}

		public boolean isMaximizeWidth() {
			return this.maximizeWidth;
		}

		public void setAlignment(double alignment) {
			this.alignment = alignment;
		}

		public void setMaximizeWidth(boolean maximizeWidth) {
			this.maximizeWidth = maximizeWidth;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o != null && this.getClass() == o.getClass()) {
				VerticalLayout.Data data = (VerticalLayout.Data) o;
				return Double.compare(data.alignment, this.alignment) == 0 && this.maximizeWidth == data.maximizeWidth;
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[] { this.alignment, this.maximizeWidth });
		}

		public String toString() {
			return "Data{alignment=" + this.alignment + ", maximizeWidth=" + this.maximizeWidth + "}";
		}
	}
}

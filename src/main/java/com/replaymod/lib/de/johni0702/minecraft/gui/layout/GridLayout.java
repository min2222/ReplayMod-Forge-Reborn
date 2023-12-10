package com.replaymod.lib.de.johni0702.minecraft.gui.layout;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public class GridLayout implements Layout {
	private static final GridLayout.Data DEFAULT_DATA = new GridLayout.Data();
	private int columns;
	private int spacingX;
	private int spacingY;
	private boolean cellsEqualSize = true;

	public Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> container,
			ReadableDimension size) {
		Preconditions.checkState(this.columns != 0, "Columns may not be 0.");
		int elements = container.getElements().size();
		int rows = (elements - 1 + this.columns) / this.columns;
		if (rows < 1) {
			return Collections.emptyMap();
		} else {
			int cellWidth = (size.getWidth() + this.spacingX) / this.columns - this.spacingX;
			int cellHeight = (size.getHeight() + this.spacingY) / rows - this.spacingY;
			Pair<int[], int[]> maxCellSize = null;
			if (!this.cellsEqualSize) {
				maxCellSize = this.calcNeededCellSize(container);
			}

			Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> map = new LinkedHashMap();
			Iterator<Entry<GuiElement, LayoutData>> iter = container.getElements().entrySet().iterator();

			for (int i = 0; i < rows; ++i) {
				for (int j = 0; j < this.columns; ++j) {
					if (!iter.hasNext()) {
						return map;
					}

					int x = j * (cellWidth + this.spacingX);
					int y = i * (cellHeight + this.spacingY);
					if (maxCellSize != null) {
						cellWidth = ((int[]) maxCellSize.getLeft())[j];
						cellHeight = ((int[]) maxCellSize.getRight())[i];
						x = 0;

						int y1;
						for (y1 = 0; y1 < j; ++y1) {
							x += ((int[]) maxCellSize.getLeft())[y1];
							x += this.spacingX;
						}

						y = 0;

						for (y1 = 0; y1 < i; ++y1) {
							y += ((int[]) maxCellSize.getRight())[y1];
							y += this.spacingY;
						}
					}

					Entry<GuiElement, LayoutData> entry = (Entry) iter.next();
					GuiElement element = (GuiElement) entry.getKey();
					GridLayout.Data data = entry.getValue() instanceof GridLayout.Data
							? (GridLayout.Data) entry.getValue()
							: DEFAULT_DATA;
					Dimension elementSize = new Dimension(element.getMinSize());
					ReadableDimension elementMaxSize = element.getMaxSize();
					elementSize.setWidth(Math.min(cellWidth, elementMaxSize.getWidth()));
					elementSize.setHeight(Math.min(cellHeight, elementMaxSize.getHeight()));
					int remainingWidth = cellWidth - elementSize.getWidth();
					int remainingHeight = cellHeight - elementSize.getHeight();
					x += (int) (data.alignmentX * (double) remainingWidth);
					y += (int) (data.alignmentY * (double) remainingHeight);
					map.put(element, Pair.of(new Point(x, y), elementSize));
				}
			}

			return map;
		}
	}

	public ReadableDimension calcMinSize(GuiContainer<?> container) {
		Preconditions.checkState(this.columns != 0, "Columns may not be 0.");
		int maxWidth = 0;
		int maxHeight = 0;
		int elements = 0;

		int height;
		for (Iterator var5 = container.getElements().entrySet().iterator(); var5.hasNext(); ++elements) {
			Entry<GuiElement, LayoutData> entry = (Entry) var5.next();
			GuiElement element = (GuiElement) entry.getKey();
			ReadableDimension minSize = element.getMinSize();
			int width = minSize.getWidth();
			if (width > maxWidth) {
				maxWidth = width;
			}

			height = minSize.getHeight();
			if (height > maxHeight) {
				maxHeight = height;
			}
		}

		int rows = (elements - 1 + this.columns) / this.columns;
		int totalWidth = maxWidth * this.columns;
		int totalHeight = maxHeight * rows;
		if (!this.cellsEqualSize) {
			Pair<int[], int[]> maxCellSize = this.calcNeededCellSize(container);
			totalWidth = 0;
			int[] var17 = (int[]) maxCellSize.getLeft();
			height = var17.length;

			int var11;
			int h;
			for (var11 = 0; var11 < height; ++var11) {
				h = var17[var11];
				totalWidth += h;
			}

			totalHeight = 0;
			var17 = (int[]) maxCellSize.getRight();
			height = var17.length;

			for (var11 = 0; var11 < height; ++var11) {
				h = var17[var11];
				totalHeight += h;
			}
		}

		if (elements > 0) {
			totalWidth += this.spacingX * (this.columns - 1);
		}

		if (elements > this.columns) {
			totalHeight += this.spacingY * (rows - 1);
		}

		return new Dimension(totalWidth, totalHeight);
	}

	private Pair<int[], int[]> calcNeededCellSize(GuiContainer<?> container) {
		int[] columnMaxWidth = new int[this.columns];
		int[] rowMaxHeight = new int[(container.getElements().size() - 1 + this.columns) / this.columns];
		int elements = 0;

		for (Iterator var5 = container.getElements().entrySet().iterator(); var5.hasNext(); ++elements) {
			Entry<GuiElement, LayoutData> entry = (Entry) var5.next();
			int column = elements % this.columns;
			int row = elements / this.columns;
			GuiElement element = (GuiElement) entry.getKey();
			ReadableDimension minSize = element.getMinSize();
			int width = minSize.getWidth();
			if (width > columnMaxWidth[column]) {
				columnMaxWidth[column] = width;
			}

			int height = minSize.getHeight();
			if (height > rowMaxHeight[row]) {
				rowMaxHeight[row] = height;
			}
		}

		return Pair.of(columnMaxWidth, rowMaxHeight);
	}

	public int getColumns() {
		return this.columns;
	}

	public int getSpacingX() {
		return this.spacingX;
	}

	public int getSpacingY() {
		return this.spacingY;
	}

	public boolean isCellsEqualSize() {
		return this.cellsEqualSize;
	}

	public GridLayout setColumns(int columns) {
		this.columns = columns;
		return this;
	}

	public GridLayout setSpacingX(int spacingX) {
		this.spacingX = spacingX;
		return this;
	}

	public GridLayout setSpacingY(int spacingY) {
		this.spacingY = spacingY;
		return this;
	}

	public GridLayout setCellsEqualSize(boolean cellsEqualSize) {
		this.cellsEqualSize = cellsEqualSize;
		return this;
	}

	public static class Data implements LayoutData {
		private double alignmentX;
		private double alignmentY;

		public Data() {
			this(0.0D, 0.0D);
		}

		public Data(double alignmentX, double alignmentY) {
			this.alignmentX = alignmentX;
			this.alignmentY = alignmentY;
		}

		public double getAlignmentX() {
			return this.alignmentX;
		}

		public double getAlignmentY() {
			return this.alignmentY;
		}

		public void setAlignmentX(double alignmentX) {
			this.alignmentX = alignmentX;
		}

		public void setAlignmentY(double alignmentY) {
			this.alignmentY = alignmentY;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o != null && this.getClass() == o.getClass()) {
				GridLayout.Data data = (GridLayout.Data) o;
				return Double.compare(data.alignmentX, this.alignmentX) == 0
						&& Double.compare(data.alignmentY, this.alignmentY) == 0;
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[] { this.alignmentX, this.alignmentY });
		}

		public String toString() {
			return "Data{alignmentX=" + this.alignmentX + ", alignmentY=" + this.alignmentY + "}";
		}
	}
}

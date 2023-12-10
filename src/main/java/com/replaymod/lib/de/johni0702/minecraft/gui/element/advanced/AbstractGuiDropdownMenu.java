package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.client.gui.Font;

public abstract class AbstractGuiDropdownMenu<V, T extends AbstractGuiDropdownMenu<V, T>>
		extends AbstractComposedGuiElement<T> implements IGuiDropdownMenu<V, T>, Clickable {
	private static final ReadableColor OUTLINE_COLOR = new Color(160, 160, 160);
	private int selected;
	private V[] values;
	private boolean opened;
	private Consumer<Integer> onSelection;
	private GuiPanel dropdown;
	private Map<V, IGuiClickable> unmodifiableDropdownEntries;
	private Function<V, String> toString = Object::toString;

	public AbstractGuiDropdownMenu() {
	}

	public AbstractGuiDropdownMenu(GuiContainer container) {
		super(container);
	}

	public int getMaxLayer() {
		return this.opened ? 1 : 0;
	}

	protected ReadableDimension calcMinSize() {
		Font fontRenderer = MCVer.getFontRenderer();
		int maxWidth = 0;
		for (V value : values) {
			int width = fontRenderer.width(toString.apply(value));
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		return new Dimension(11 + maxWidth + fontRenderer.lineHeight, fontRenderer.lineHeight + 4);
	}

	public void layout(ReadableDimension size, RenderInfo renderInfo) {
		super.layout(size, renderInfo);
		Font fontRenderer = MCVer.getFontRenderer();
		if (renderInfo.layer == 1) {
			ReadablePoint offsetPoint = new Point(0, size.getHeight());
			int var10002 = size.getWidth();
			Objects.requireNonNull(fontRenderer);
			ReadableDimension offsetSize = new Dimension(var10002, (9 + 5) * this.values.length);
			this.dropdown.layout(offsetSize, renderInfo.offsetMouse(0, offsetPoint.getY()).layer(0));
		}

	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		Font fontRenderer = MCVer.getFontRenderer();
		int var10002;
		if (renderInfo.layer == 0) {
			int width = size.getWidth();
			int height = size.getHeight();
			renderer.drawRect(0, 0, width, height, OUTLINE_COLOR);
			renderer.drawRect(1, 1, width - 2, height - 2, ReadableColor.BLACK);
			renderer.drawRect(width - height, 0, 1, height, OUTLINE_COLOR);
			int base = height - 6;
			int tHeight = base / 2;
			int x = width - 3 - base / 2;
			int y = height / 2 - 2;

			for (int layer = tHeight; layer > 0; --layer) {
				renderer.drawRect(x - layer, y + (tHeight - layer), layer * 2 - 1, 1, OUTLINE_COLOR);
			}

			var10002 = height / 2;
			Objects.requireNonNull(fontRenderer);
			renderer.drawString(3, var10002 - 9 / 2, ReadableColor.WHITE,
					(String) this.toString.apply(this.getSelectedValue()));
		} else if (renderInfo.layer == 1) {
			ReadablePoint offsetPoint = new Point(0, size.getHeight());
			var10002 = size.getWidth();
			Objects.requireNonNull(fontRenderer);
			ReadableDimension offsetSize = new Dimension(var10002, (9 + 5) * this.values.length);
			OffsetGuiRenderer offsetRenderer = new OffsetGuiRenderer(renderer, offsetPoint, offsetSize);
			offsetRenderer.startUsing();

			try {
				this.dropdown.draw(offsetRenderer, offsetSize, renderInfo.offsetMouse(0, offsetPoint.getY()).layer(0));
			} finally {
				offsetRenderer.stopUsing();
			}
		}

	}

	public T setValues(V... values) {
		this.values = values;
		dropdown = new GuiPanel() {
			@Override
			public void convertFor(GuiElement element, Point point, int relativeLayer) {
				AbstractGuiDropdownMenu parent = AbstractGuiDropdownMenu.this;
				if (parent.getContainer() != null) {
					parent.getContainer().convertFor(parent, point, relativeLayer + 1);
				}
				point.translate(0, -AbstractGuiDropdownMenu.this.getLastSize().getHeight());
				super.convertFor(element, point, relativeLayer);
			}
		}.setLayout(new VerticalLayout());
		Map<V, IGuiClickable> dropdownEntries = new LinkedHashMap<>();
		for (V value : values) {
			DropdownEntry entry = new DropdownEntry(value);
			dropdownEntries.put(value, entry);
			dropdown.addElements(null, entry);
		}
		unmodifiableDropdownEntries = Collections.unmodifiableMap(dropdownEntries);
		return getThis();
	}

	public T setSelected(int selected) {
		this.selected = selected;
		this.onSelection(selected);
		return this.getThis();
	}

	public T setSelected(V value) {
		for (int i = 0; i < this.values.length; ++i) {
			if (this.values[i].equals(value)) {
				return this.setSelected(i);
			}
		}

		throw new IllegalArgumentException("The value " + value + " is not in this dropdown menu.");
	}

	public V getSelectedValue() {
		return this.values[this.selected];
	}

	public T setOpened(boolean opened) {
		this.opened = opened;
		return this.getThis();
	}

	public Collection<GuiElement> getChildren() {
		return this.opened ? Collections.singletonList(this.dropdown) : Collections.emptyList();
	}

	public T onSelection(Consumer<Integer> consumer) {
		this.onSelection = consumer;
		return this.getThis();
	}

	public void onSelection(Integer value) {
		if (this.onSelection != null) {
			this.onSelection.consume(value);
		}

	}

	public boolean mouseClick(ReadablePoint position, int button) {
		Point pos = new Point(position);
		if (this.getContainer() != null) {
			this.getContainer().convertFor(this, pos);
		}

		if (this.isEnabled() && this.isMouseHovering(pos)) {
			this.setOpened(!this.isOpened());
			return true;
		} else {
			return false;
		}
	}

	protected boolean isMouseHovering(ReadablePoint pos) {
		return pos.getX() > 0 && pos.getY() > 0 && pos.getX() < this.getLastSize().getWidth()
				&& pos.getY() < this.getLastSize().getHeight();
	}

	public Map<V, IGuiClickable> getDropdownEntries() {
		return this.unmodifiableDropdownEntries;
	}

	public T setToString(Function<V, String> toString) {
		this.toString = toString;
		return this.getThis();
	}

	public int getSelected() {
		return this.selected;
	}

	public V[] getValues() {
		return this.values;
	}

	public boolean isOpened() {
		return this.opened;
	}

	private class DropdownEntry extends AbstractGuiClickable<AbstractGuiDropdownMenu<V, T>.DropdownEntry> {
		private final V value;

		public DropdownEntry(V value) {
			this.value = value;
		}

		protected AbstractGuiDropdownMenu<V, T>.DropdownEntry getThis() {
			return this;
		}

		protected ReadableDimension calcMinSize() {
			Objects.requireNonNull(MCVer.getFontRenderer());
			return new Dimension(0, 9 + 5);
		}

		public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
			super.draw(renderer, size, renderInfo);
			int width = size.getWidth();
			int height = size.getHeight();
			renderer.drawRect(0, 0, width, height, AbstractGuiDropdownMenu.OUTLINE_COLOR);
			renderer.drawRect(1, 0, width - 2, height - 1, ReadableColor.BLACK);
			renderer.drawString(3, 2, ReadableColor.WHITE,
					(String) AbstractGuiDropdownMenu.this.toString.apply(this.value));
		}

		public boolean mouseClick(ReadablePoint position, int button) {
			boolean result = super.mouseClick(position, button);
			AbstractGuiDropdownMenu.this.setOpened(false);
			return result;
		}

		protected void onClick() {
			AbstractGuiDropdownMenu.this.setSelected(this.value);
		}
	}
}

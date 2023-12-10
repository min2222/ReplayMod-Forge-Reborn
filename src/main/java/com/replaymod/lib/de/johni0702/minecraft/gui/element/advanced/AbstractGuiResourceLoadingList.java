package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Loadable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

import net.minecraft.client.gui.screens.Screen;

public abstract class AbstractGuiResourceLoadingList<T extends AbstractGuiResourceLoadingList<T, U>, U extends GuiElement<U> & Comparable<U>>
		extends AbstractGuiVerticalList<T> implements Tickable, Loadable, Closeable, Typeable {
	private static final String[] LOADING_TEXT = new String[] { "Ooo", "oOo", "ooO", "oOo" };
	private final GuiLabel loadingElement = new GuiLabel();
	private final GuiPanel resourcesPanel = (GuiPanel) (new GuiPanel(this.getListPanel()))
			.setLayout(new VerticalLayout());
	private final Queue<Runnable> resourcesQueue = new ConcurrentLinkedQueue();
	private Consumer<Consumer<Supplier<U>>> onLoad;
	private Runnable onSelectionChanged;
	private Runnable onSelectionDoubleClicked;
	private Thread loaderThread;
	private int tick;
	private final List<AbstractGuiResourceLoadingList<T, U>.Element> selected = new ArrayList();
	private long selectedLastClickTime;

	public AbstractGuiResourceLoadingList() {
	}

	public AbstractGuiResourceLoadingList(GuiContainer container) {
		super(container);
	}

	public void tick() {
		this.loadingElement.setText(LOADING_TEXT[this.tick++ / 5 % LOADING_TEXT.length]);

		Runnable resource;
		while ((resource = (Runnable) this.resourcesQueue.poll()) != null) {
			resource.run();
		}

	}

	@Override
	public void load() {
		// Stop current loading
		if (loaderThread != null) {
			loaderThread.interrupt();
			try {
				loaderThread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}

		// Clear list
		resourcesQueue.clear();
		for (GuiElement element : new ArrayList<>(resourcesPanel.getChildren())) {
			resourcesPanel.removeElement(element);
		}
		selected.clear();
		onSelectionChanged();

		// Load new data
		loaderThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					onLoad.consume(new Consumer<Supplier<U>>() {
						@Override
						public void consume(final Supplier<U> obj) {
							resourcesQueue.offer(new Runnable() {
								@Override
								public void run() {
									resourcesPanel.addElements(null, new Element(obj.get()));
									resourcesPanel.sortElements();
								}
							});
						}
					});
				} finally {
					resourcesQueue.offer(new Runnable() {
						@Override
						public void run() {
							getListPanel().removeElement(loadingElement);
						}
					});
				}
			}
		});
		getListPanel().addElements(new VerticalLayout.Data(0.5), loadingElement);
		loaderThread.start();
	}

	public void close() {
		this.loaderThread.interrupt();
	}

	public T onLoad(Consumer<Consumer<Supplier<U>>> function) {
		this.onLoad = function;
		return this.getThis();
	}

	public void onSelectionChanged() {
		if (this.onSelectionChanged != null) {
			this.onSelectionChanged.run();
		}

	}

	public void onSelectionDoubleClicked() {
		if (this.onSelectionDoubleClicked != null) {
			this.onSelectionDoubleClicked.run();
		}

	}

	public T onSelectionChanged(Runnable onSelectionChanged) {
		this.onSelectionChanged = onSelectionChanged;
		return this.getThis();
	}

	public T onSelectionDoubleClicked(Runnable onSelectionDoubleClicked) {
		this.onSelectionDoubleClicked = onSelectionDoubleClicked;
		return this.getThis();
	}

	public List<U> getSelected() {
		List<U> selectedResources = new ArrayList(this.selected.size());
		Iterator var2 = this.selected.iterator();

		while (var2.hasNext()) {
			AbstractGuiResourceLoadingList<T, U>.Element element = (AbstractGuiResourceLoadingList.Element) var2.next();
			selectedResources.add(element.resource);
		}

		return selectedResources;
	}

	public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
			boolean shiftDown) {
		if (Screen.hasControlDown() && keyCode == 65) {
			List<AbstractGuiResourceLoadingList<T, U>.Element> all = new ArrayList();
			Iterator var7 = this.getListPanel().getChildren().iterator();

			while (var7.hasNext()) {
				GuiElement<?> child = (GuiElement) var7.next();
				if (child instanceof AbstractGuiResourceLoadingList.Element) {
					all.add((AbstractGuiResourceLoadingList.Element) child);
				}
			}

			if (this.selected.size() < all.size()) {
				this.selected.clear();
				this.selected.addAll(all);
			} else {
				this.selected.clear();
			}

			this.onSelectionChanged();
			return true;
		} else {
			return false;
		}
	}

	private class Element extends GuiPanel
			implements Clickable, Comparable<AbstractGuiResourceLoadingList<T, U>.Element> {
		private final U resource;

		public Element(U resource) {
			this.resource = resource;
			addElements(null, resource);
			setLayout(new CustomLayout<GuiPanel>() {
				@Override
				protected void layout(GuiPanel container, int width, int height) {
					pos(resource, 2, 2);
				}

				@Override
				public ReadableDimension calcMinSize(GuiContainer<?> container) {
					ReadableDimension size = resource.getMinSize();
					return new Dimension(size.getWidth() + 4, size.getHeight() + 4);
				}
			});
		}

		public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
			if (renderInfo.layer == 0 && AbstractGuiResourceLoadingList.this.selected.contains(this)) {
				int w = size.getWidth();
				int h = size.getHeight();
				renderer.drawRect(0, 0, w, h, Colors.BLACK);
				renderer.drawRect(0, 0, w, 1, Colors.LIGHT_GRAY);
				renderer.drawRect(0, h - 1, w, 1, Colors.LIGHT_GRAY);
				renderer.drawRect(0, 0, 1, h, Colors.LIGHT_GRAY);
				renderer.drawRect(w - 1, 0, 1, h, Colors.LIGHT_GRAY);
			}

			super.draw(renderer, size, renderInfo);
		}

		public boolean mouseClick(ReadablePoint position, int button) {
			Point point = new Point(position);
			this.getContainer().convertFor(this, point);
			if (point.getX() > 0 && point.getX() < this.getLastSize().getWidth() && point.getY() > 0
					&& point.getY() < this.getLastSize().getHeight()) {
				if (Screen.hasControlDown()) {
					if (AbstractGuiResourceLoadingList.this.selected.contains(this)) {
						AbstractGuiResourceLoadingList.this.selected.remove(this);
					} else {
						AbstractGuiResourceLoadingList.this.selected.add(this);
					}

					AbstractGuiResourceLoadingList.this.onSelectionChanged();
				} else if (AbstractGuiResourceLoadingList.this.selected.contains(this) && System.currentTimeMillis()
						- AbstractGuiResourceLoadingList.this.selectedLastClickTime < 250L) {
					AbstractGuiResourceLoadingList.this.onSelectionDoubleClicked();
				} else {
					AbstractGuiResourceLoadingList.this.selected.clear();
					AbstractGuiResourceLoadingList.this.selected.add(this);
					AbstractGuiResourceLoadingList.this.onSelectionChanged();
				}

				AbstractGuiResourceLoadingList.this.selectedLastClickTime = System.currentTimeMillis();
				return true;
			} else {
				return false;
			}
		}

		public int compareTo(AbstractGuiResourceLoadingList<T, U>.Element o) {
			return ((Comparable) this.resource).compareTo(o.resource);
		}
	}
}

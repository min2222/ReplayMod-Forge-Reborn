package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public abstract class AbstractComposedGuiElement<T extends AbstractComposedGuiElement<T>> extends AbstractGuiElement<T>
		implements ComposedGuiElement<T> {
	public AbstractComposedGuiElement() {
	}

	public AbstractComposedGuiElement(GuiContainer container) {
		super(container);
	}

	public int getMaxLayer() {
		return this.getLayer() + this.getChildren().stream().mapToInt((e) -> {
			return e instanceof ComposedGuiElement ? ((ComposedGuiElement) e).getMaxLayer() : e.getLayer();
		}).max().orElse(0);
	}

	@Override
	public <C, R> R forEach(int layer, Class<C> ofType, BiFunction<ComposedGuiElement<?>, Integer, R> recurse,
			Function<C, R> function) {
		if (ofType.isInstance(this) && getLayer() == layer) {
			R result = function.apply(ofType.cast(this));
			if (result != null) {
				return result;
			}
		}
		for (final GuiElement<?> element : getChildren()) {
			R result = null;
			if (element instanceof ComposedGuiElement) {
				ComposedGuiElement<?> composed = (ComposedGuiElement<?>) element;
				if (layer <= composed.getMaxLayer()) {
					result = recurse.apply(composed, layer - composed.getLayer());
				}
			} else if (ofType.isInstance(element) && element.getLayer() == layer) {
				result = function.apply(ofType.cast(element));
			}
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}

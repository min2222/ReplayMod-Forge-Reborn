package com.replaymod.lib.de.johni0702.minecraft.gui.popup;

import java.util.function.Consumer;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public class GuiYesNoPopup extends AbstractGuiPopup<GuiYesNoPopup> implements Typeable {
	private Consumer<Boolean> onClosed = (accepted) -> {
	};
	private Runnable onAccept = () -> {
	};
	private Runnable onReject = () -> {
	};
	private final GuiButton yesButton = (GuiButton) ((GuiButton) (new GuiButton()).setSize(150, 20))
			.onClick(new Runnable() {
				public void run() {
					GuiYesNoPopup.this.close();
					GuiYesNoPopup.this.onAccept.run();
					GuiYesNoPopup.this.onClosed.accept(true);
				}
			});
	private final GuiButton noButton = (GuiButton) ((GuiButton) (new GuiButton()).setSize(150, 20))
			.onClick(new Runnable() {
				public void run() {
					GuiYesNoPopup.this.close();
					GuiYesNoPopup.this.onReject.run();
					GuiYesNoPopup.this.onClosed.accept(false);
				}
			});
	private final GuiPanel info;
	private final GuiPanel buttons;
	private int layer;

	public static GuiYesNoPopup open(GuiContainer container, GuiElement... info) {
		GuiYesNoPopup popup = (GuiYesNoPopup) (new GuiYesNoPopup(container))
				.setBackgroundColor(Colors.DARK_TRANSPARENT);
		popup.getInfo().addElements(new VerticalLayout.Data(0.5D), info);
		popup.open();
		return popup;
	}

	public GuiYesNoPopup(GuiContainer container) {
		super(container);
		this.info = (GuiPanel) ((GuiPanel) (new GuiPanel()).setMinSize(new Dimension(320, 50)))
				.setLayout((new VerticalLayout(VerticalLayout.Alignment.TOP)).setSpacing(2));
		this.buttons = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout(HorizontalLayout.Alignment.CENTER)).setSpacing(5)))
				.addElements(new HorizontalLayout.Data(0.5D), new GuiElement[] { this.yesButton, this.noButton });
		((GuiPanel) this.popup.setLayout((new VerticalLayout()).setSpacing(10)))
				.addElements(new VerticalLayout.Data(0.5D), new GuiElement[] { this.info, this.buttons });
	}

	public GuiYesNoPopup setYesLabel(String label) {
		this.yesButton.setLabel(label);
		return this;
	}

	public GuiYesNoPopup setNoLabel(String label) {
		this.noButton.setLabel(label);
		return this;
	}

	public GuiYesNoPopup setYesI18nLabel(String label, Object... args) {
		this.yesButton.setI18nLabel(label, args);
		return this;
	}

	public GuiYesNoPopup setNoI18nLabel(String label, Object... args) {
		this.noButton.setI18nLabel(label, args);
		return this;
	}

	public GuiYesNoPopup onClosed(Consumer<Boolean> onClosed) {
		this.onClosed = onClosed;
		return this;
	}

	public GuiYesNoPopup onAccept(Runnable onAccept) {
		this.onAccept = onAccept;
		return this;
	}

	public GuiYesNoPopup onReject(Runnable onReject) {
		this.onReject = onReject;
		return this;
	}

	protected GuiYesNoPopup getThis() {
		return this;
	}

	public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
			boolean shiftDown) {
		if (keyCode == 256) {
			this.noButton.onClick();
			return true;
		} else {
			return false;
		}
	}

	public GuiButton getYesButton() {
		return this.yesButton;
	}

	public GuiButton getNoButton() {
		return this.noButton;
	}

	public GuiPanel getInfo() {
		return this.info;
	}

	public GuiPanel getButtons() {
		return this.buttons;
	}

	public int getLayer() {
		return this.layer;
	}

	public GuiYesNoPopup setLayer(int layer) {
		this.layer = layer;
		return this;
	}
}

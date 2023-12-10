package com.replaymod.extras.advancedscreenshots;

import java.net.URI;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.versions.MCVer;
import com.replaymod.extras.Setting;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiCheckbox;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.render.RenderSettings;

public class GuiUploadScreenshot extends AbstractGuiPopup<GuiUploadScreenshot> {
	public final ReplayMod mod;
	public final RenderSettings renderSettings;
	public final GuiLabel successLabel;
	public final GuiLabel veerLabel;
	public final GuiButton veerUploadButton;
	public final GuiButton showOnDiskButton;
	public final GuiButton closeButton;
	public final GuiCheckbox neverOpenCheckbox;
	public final GuiLabel neverOpenLabel;
	public final GuiPanel checkboxPanel;

	public GuiUploadScreenshot(GuiContainer container, ReplayMod mod, RenderSettings renderSettings) {
		super(container);
		this.successLabel = (GuiLabel) ((GuiLabel) (new GuiLabel())
				.setI18nText("replaymod.gui.advancedscreenshots.finished.description", new Object[0]))
				.setColor(ReadableColor.BLACK);
		this.veerLabel = (GuiLabel) ((GuiLabel) (new GuiLabel())
				.setI18nText("replaymod.gui.advancedscreenshots.finished.description.veer", new Object[0]))
				.setColor(ReadableColor.BLACK);
		this.veerUploadButton = (GuiButton) ((GuiButton) (new GuiButton()).setSize(150, 20))
				.setI18nLabel("replaymod.gui.advancedscreenshots.finished.upload.veer", new Object[0]);
		this.showOnDiskButton = (GuiButton) ((GuiButton) (new GuiButton()).setSize(150, 20))
				.setI18nLabel("replaymod.gui.advancedscreenshots.finished.showfile", new Object[0]);
		this.closeButton = (GuiButton) ((GuiButton) (new GuiButton()).setSize(150, 20))
				.setI18nLabel("replaymod.gui.close", new Object[0]);
		this.neverOpenCheckbox = new GuiCheckbox();
		this.neverOpenLabel = (GuiLabel) ((GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.notagain",
				new Object[0])).setColor(ReadableColor.BLACK);
		this.checkboxPanel = GuiPanel.builder()
				.layout((new HorizontalLayout(HorizontalLayout.Alignment.RIGHT)).setSpacing(5))
				.with(this.neverOpenCheckbox, new HorizontalLayout.Data(0.5D))
				.with(this.neverOpenLabel, new HorizontalLayout.Data(0.5D)).build();
		this.mod = mod;
		this.renderSettings = renderSettings;
		boolean veer = renderSettings.getRenderMethod() == RenderSettings.RenderMethod.EQUIRECTANGULAR;
		if (renderSettings.getRenderMethod() == RenderSettings.RenderMethod.EQUIRECTANGULAR) {
			this.successLabel.setI18nText("replaymod.gui.advancedscreenshots.finished.description.360", new Object[0]);
		}

		if (veer) {
			this.veerUploadButton.onClick(() -> {
				MCVer.openURL(URI.create("https://veer.tv/upload"));
			});
		}

		this.showOnDiskButton.onClick(() -> {
			MCVer.openFile(renderSettings.getOutputFile().getParentFile());
		});
		this.closeButton.onClick(() -> {
			if (this.neverOpenCheckbox.isChecked()) {
				SettingsRegistry settingsRegistry = mod.getSettingsRegistry();
				settingsRegistry.set(Setting.SKIP_POST_SCREENSHOT_GUI, true);
				settingsRegistry.save();
			}

			this.close();
		});
		this.popup.addElements(new VerticalLayout.Data(0.5D), new GuiElement[] { this.successLabel });
		if (veer) {
			this.popup.addElements(new VerticalLayout.Data(0.5D),
					new GuiElement[] { this.veerLabel, this.veerUploadButton });
		}

		this.popup.addElements(new VerticalLayout.Data(0.5D),
				new GuiElement[] { this.successLabel, this.showOnDiskButton, this.closeButton });
		this.popup.addElements(new VerticalLayout.Data(1.0D), new GuiElement[] { this.checkboxPanel });
		this.popup.setLayout((new VerticalLayout()).setSpacing(5));
	}

	protected void open() {
		if (!(Boolean) this.mod.getSettingsRegistry().get(Setting.SKIP_POST_SCREENSHOT_GUI)) {
			super.open();
		}
	}

	protected GuiUploadScreenshot getThis() {
		return this;
	}
}

package com.replaymod.replay.gui.screen;

import java.util.Iterator;
import java.util.Map.Entry;

import com.replaymod.core.utils.ModCompat;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.replaystudio.data.ModInfo;
import com.replaymod.replaystudio.util.I18n;

import net.minecraft.client.gui.screens.Screen;

public class GuiModCompatWarning extends AbstractGuiScreen<GuiModCompatWarning> {
	public final GuiVerticalList content = (GuiVerticalList) ((GuiVerticalList) (new GuiVerticalList(this))
			.setDrawShadow(true)).setDrawSlider(true);
	public final GuiButton loadButton = (GuiButton) ((GuiButton) (new GuiButton()).setI18nLabel("replaymod.gui.load",
			new Object[0])).setSize(200, 20);
	public final GuiButton cancelButton = (GuiButton) ((GuiButton) (new GuiButton()).setI18nLabel("gui.cancel",
			new Object[0])).setSize(200, 20);
	public final GuiPanel closeButtons;

	public GuiModCompatWarning(ModCompat.ModInfoDifference difference) {
		this.closeButtons = (GuiPanel) ((GuiPanel) (new GuiPanel(this))
				.setLayout((new HorizontalLayout()).setSpacing(5)))
				.addElements((LayoutData) null, new GuiElement[] { this.loadButton, this.cancelButton });
		this.setTitle((GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.modwarning.title", new Object[0]));
		this.setLayout(new CustomLayout<GuiModCompatWarning>() {
			protected void layout(GuiModCompatWarning container, int width, int height) {
				this.pos(GuiModCompatWarning.this.content, 10, 35);
				this.pos(GuiModCompatWarning.this.closeButtons,
						width / 2 - this.width(GuiModCompatWarning.this.closeButtons) / 2,
						height - 10 - this.height(GuiModCompatWarning.this.closeButtons));
				this.size(GuiModCompatWarning.this.content, width - 20,
						this.y(GuiModCompatWarning.this.closeButtons) - 10 - this.y(GuiModCompatWarning.this.content));
			}
		});
		this.content.getListLayout().setSpacing(8);
		VerticalLayout.Data data = new VerticalLayout.Data(0.5D);
		GuiPanel content = this.content.getListPanel();
		content.addElements(data,
				new GuiElement[] { (new GuiLabel()).setI18nText("replaymod.gui.modwarning.message1", new Object[0]) });
		content.addElements(data,
				new GuiElement[] { (new GuiLabel()).setI18nText("replaymod.gui.modwarning.message2", new Object[0]) });
		content.addElements(data,
				new GuiElement[] { (new GuiLabel()).setI18nText("replaymod.gui.modwarning.message3", new Object[0]) });
		GuiElement[] var10002;
		Iterator var4;
		GuiPanel var10005;
		GuiElement[] var10007;
		GuiLabel var10010;
		String var10011;
		if (!difference.getMissing().isEmpty()) {
			content.addElements(data, new GuiElement[] { new GuiLabel() });
			content.addElements(data, new GuiElement[] {
					(new GuiLabel()).setI18nText("replaymod.gui.modwarning.missing", new Object[0]) });
			var4 = difference.getMissing().iterator();

			while (var4.hasNext()) {
				ModInfo modInfo = (ModInfo) var4.next();
				var10002 = new GuiElement[1];
				var10005 = (GuiPanel) (new GuiPanel()).setLayout((new VerticalLayout()).setSpacing(3));
				var10007 = new GuiElement[] {
						GuiPanel.builder().layout((new HorizontalLayout()).setSpacing(3))
								.with((new GuiLabel()).setI18nText("replaymod.gui.modwarning.name", new Object[0]),
										(LayoutData) null)
								.with((new GuiLabel()).setText(modInfo.getName()), (LayoutData) null).build(),
						GuiPanel.builder().layout((new HorizontalLayout()).setSpacing(3))
								.with((new GuiLabel()).setI18nText("replaymod.gui.modwarning.id", new Object[0]),
										(LayoutData) null)
								.with((new GuiLabel()).setText(modInfo.getId()), (LayoutData) null).build(),
						null };
				var10010 = new GuiLabel();
				var10011 = I18n.format("replaymod.gui.modwarning.version.expected");
				var10007[2] = var10010.setText(var10011 + ": " + modInfo.getVersion());
				var10002[0] = var10005.addElements((LayoutData) null, var10007);
				content.addElements(data, var10002);
			}
		}

		if (!difference.getDiffering().isEmpty()) {
			content.addElements(data, new GuiElement[] { new GuiLabel() });
			content.addElements(data, new GuiElement[] {
					(new GuiLabel()).setI18nText("replaymod.gui.modwarning.version", new Object[0]) });
			var4 = difference.getDiffering().entrySet().iterator();

			while (var4.hasNext()) {
				Entry<ModInfo, String> entry = (Entry) var4.next();
				var10002 = new GuiElement[1];
				var10005 = (GuiPanel) (new GuiPanel()).setLayout((new VerticalLayout()).setSpacing(3));
				var10007 = new GuiElement[] {
						GuiPanel.builder().layout((new HorizontalLayout()).setSpacing(3))
								.with((new GuiLabel()).setI18nText("replaymod.gui.modwarning.name", new Object[0]),
										(LayoutData) null)
								.with((new GuiLabel()).setText(((ModInfo) entry.getKey()).getName()), (LayoutData) null)
								.build(),
						GuiPanel.builder().layout((new HorizontalLayout()).setSpacing(3))
								.with((new GuiLabel()).setI18nText("replaymod.gui.modwarning.id", new Object[0]),
										(LayoutData) null)
								.with((new GuiLabel()).setText(((ModInfo) entry.getKey()).getId()), (LayoutData) null)
								.build(),
						null };
				var10010 = new GuiLabel();
				var10011 = I18n.format("replaymod.gui.modwarning.version.expected");
				var10007[2] = var10010.setText(var10011 + ": " + ((ModInfo) entry.getKey()).getVersion() + ", "
						+ I18n.format("replaymod.gui.modwarning.version.found") + ": " + (String) entry.getValue());
				var10002[0] = var10005.addElements((LayoutData) null, var10007);
				content.addElements(data, var10002);
			}
		}

		this.cancelButton.onClick(() -> {
			this.getMinecraft().setScreen((Screen) null);
		});
	}

	protected GuiModCompatWarning getThis() {
		return this;
	}
}

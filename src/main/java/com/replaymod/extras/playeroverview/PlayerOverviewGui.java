package com.replaymod.extras.playeroverview;

import java.io.Closeable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiCheckbox;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiImage;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiCheckbox;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.replay.ReplayModReplay;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public class PlayerOverviewGui extends GuiScreen implements Closeable {
	protected static final int ENTRY_WIDTH = 200;
	public final GuiPanel contentPanel;
	public final GuiLabel spectateLabel;
	public final GuiLabel visibleLabel;
	public final GuiVerticalList playersScrollable;
	public final GuiCheckbox saveCheckbox;
	public final GuiCheckbox checkAll;
	public final GuiCheckbox uncheckAll;
	private final PlayerOverview extra;

	public PlayerOverviewGui(PlayerOverview extra, List<Player> players) {
		this.contentPanel = (GuiPanel) (new GuiPanel(this)).setBackgroundColor(Colors.DARK_TRANSPARENT);
		this.spectateLabel = (GuiLabel) (new GuiLabel(this.contentPanel))
				.setI18nText("replaymod.gui.playeroverview.spectate", new Object[0]);
		this.visibleLabel = (GuiLabel) (new GuiLabel(this.contentPanel))
				.setI18nText("replaymod.gui.playeroverview.visible", new Object[0]);
		this.playersScrollable = (GuiVerticalList) ((GuiVerticalList) (new GuiVerticalList(this.contentPanel))
				.setDrawSlider(true)).setDrawShadow(true);
		this.saveCheckbox = (GuiCheckbox) ((GuiCheckbox) (new GuiCheckbox(this.contentPanel))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.playeroverview.remembersettings.description",
						new Object[0])))
				.setI18nLabel("replaymod.gui.playeroverview.remembersettings", new Object[0]);
		this.checkAll = (GuiCheckbox) ((GuiCheckbox) ((GuiCheckbox) (new GuiCheckbox(this.contentPanel) {
			public void onClick() {
				PlayerOverviewGui.this.playersScrollable.invokeAll(IGuiCheckbox.class, (e) -> {
					e.setChecked(true);
				});
			}
		}).setLabel("")).setChecked(true))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.playeroverview.showall", new Object[0]));
		this.uncheckAll = (GuiCheckbox) ((GuiCheckbox) ((GuiCheckbox) (new GuiCheckbox(this.contentPanel) {
			public void onClick() {
				PlayerOverviewGui.this.playersScrollable.invokeAll(IGuiCheckbox.class, (e) -> {
					e.setChecked(false);
				});
			}
		}).setLabel("")).setChecked(false))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.playeroverview.hideall", new Object[0]));
		this.setBackground(AbstractGuiScreen.Background.NONE);
		this.setTitle((GuiLabel) (new GuiLabel()).setI18nText("replaymod.input.playeroverview", new Object[0]));
		this.setLayout(new CustomLayout<GuiScreen>() {
			protected void layout(GuiScreen container, int width, int height) {
				this.size(PlayerOverviewGui.this.contentPanel, 230, height - 40);
				this.pos(PlayerOverviewGui.this.contentPanel,
						width / 2 - this.width(PlayerOverviewGui.this.contentPanel) / 2, 20);
			}
		});
		this.contentPanel.setLayout(new CustomLayout<GuiPanel>() {
			protected void layout(GuiPanel container, int width, int height) {
				this.pos(PlayerOverviewGui.this.spectateLabel, 10, 10);
				this.pos(PlayerOverviewGui.this.visibleLabel,
						width - 10 - this.width(PlayerOverviewGui.this.visibleLabel), 10);
				this.pos(PlayerOverviewGui.this.playersScrollable, 10, this.y(PlayerOverviewGui.this.spectateLabel)
						+ this.height(PlayerOverviewGui.this.spectateLabel) + 5);
				this.size(PlayerOverviewGui.this.playersScrollable, width - 10 - 5,
						height - 15 - this.height(PlayerOverviewGui.this.saveCheckbox)
								- this.y(PlayerOverviewGui.this.playersScrollable));
				this.pos(PlayerOverviewGui.this.saveCheckbox, 10,
						height - 10 - this.height(PlayerOverviewGui.this.saveCheckbox));
				this.pos(PlayerOverviewGui.this.uncheckAll, width - this.width(PlayerOverviewGui.this.uncheckAll) - 8,
						height - this.height(PlayerOverviewGui.this.uncheckAll) - 10);
				this.pos(PlayerOverviewGui.this.checkAll,
						this.x(PlayerOverviewGui.this.uncheckAll) - 3 - this.width(PlayerOverviewGui.this.checkAll),
						this.y(PlayerOverviewGui.this.uncheckAll));
			}
		});
		this.extra = extra;
		Collections.sort(players, new PlayerOverviewGui.PlayerComparator());
		Iterator var3 = players.iterator();

		while (var3.hasNext()) {
			final Player p = (Player) var3.next();
			if (p instanceof AbstractClientPlayer) {
				final ResourceLocation texture = ((AbstractClientPlayer) p).getSkinTextureLocation();
				final GuiClickable panel = (GuiClickable) ((GuiClickable) ((GuiClickable) (new GuiClickable())
						.setLayout((new HorizontalLayout()).setSpacing(2)))
						.addElements(new HorizontalLayout.Data(0.5D), new GuiElement[] { (new GuiImage() {
							public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
								renderer.bindTexture(texture);
								renderer.drawTexturedRect(0, 0, 8, 8, 16, 16, 8, 8, 64, 64);
								if (p.isModelPartShown(PlayerModelPart.HAT)) {
									renderer.drawTexturedRect(0, 0, 40, 8, size.getWidth(), size.getHeight(), 8, 8, 64,
											64);
								}

							}
						}).setSize(16, 16),
								((GuiLabel) (new GuiLabel()).setText(p.getName().getString()))
										.setColor(isSpectator(p) ? Colors.DKGREY : Colors.WHITE) }))
						.onClick(new Runnable() {
							public void run() {
								ReplayModReplay.instance.getReplayHandler().spectateEntity(p);
							}
						});
				final GuiCheckbox checkbox = (new GuiCheckbox() {
					public GuiCheckbox setChecked(boolean checked) {
						extra.setHidden(p.getUUID(), !checked);
						return (GuiCheckbox) super.setChecked(checked);
					}
				}).setChecked(!extra.isHidden(p.getUUID()));
				((GuiPanel) (new GuiPanel(this.playersScrollable.getListPanel()))
						.setLayout(new CustomLayout<GuiPanel>() {
							protected void layout(GuiPanel container, int width, int height) {
								this.pos(panel, 5, 0);
								this.pos(checkbox, width - this.width(checkbox) - 5,
										height / 2 - this.height(checkbox) / 2);
							}

							public ReadableDimension calcMinSize(GuiContainer<?> container) {
								return new Dimension(200, panel.getMinSize().getHeight());
							}
						})).addElements((LayoutData) null, new GuiElement[] { panel, checkbox });
			}
		}

		((GuiCheckbox) this.saveCheckbox.setChecked(extra.isSavingEnabled())).onClick(new Runnable() {
			public void run() {
				extra.setSavingEnabled(PlayerOverviewGui.this.saveCheckbox.isChecked());
			}
		});
		ReplayModReplay.instance.getReplayHandler().getOverlay().setVisible(false);
	}

	public void close() {
		ReplayModReplay.instance.getReplayHandler().getOverlay().setVisible(true);
		this.extra.saveHiddenPlayers();
	}

	private static boolean isSpectator(Player e) {
		return e.isInvisible() && e.getEffect(MobEffects.INVISIBILITY) == null;
	}

	private static final class PlayerComparator implements Comparator<Player> {
		public int compare(Player o1, Player o2) {
			if (PlayerOverviewGui.isSpectator(o1) && !PlayerOverviewGui.isSpectator(o2)) {
				return 1;
			} else {
				return PlayerOverviewGui.isSpectator(o2) && !PlayerOverviewGui.isSpectator(o1) ? -1
						: o1.getName().getString().compareToIgnoreCase(o2.getName().getString());
			}
		}
	}
}

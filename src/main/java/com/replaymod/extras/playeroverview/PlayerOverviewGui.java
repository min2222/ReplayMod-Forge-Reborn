package com.replaymod.extras.playeroverview;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.replaymod.core.utils.Utils;
import com.replaymod.gui.GuiRenderer;
import com.replaymod.gui.RenderInfo;
import com.replaymod.gui.container.GuiClickable;
import com.replaymod.gui.container.GuiContainer;
import com.replaymod.gui.container.GuiPanel;
import com.replaymod.gui.container.GuiScreen;
import com.replaymod.gui.container.GuiVerticalList;
import com.replaymod.gui.element.GuiCheckbox;
import com.replaymod.gui.element.GuiImage;
import com.replaymod.gui.element.GuiLabel;
import com.replaymod.gui.element.GuiTooltip;
import com.replaymod.gui.element.IGuiCheckbox;
import com.replaymod.gui.function.Closeable;
import com.replaymod.gui.layout.CustomLayout;
import com.replaymod.gui.layout.HorizontalLayout;
import com.replaymod.gui.utils.Colors;
import com.replaymod.replay.ReplayModReplay;

import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public class PlayerOverviewGui extends GuiScreen implements Closeable {
    protected static final int ENTRY_WIDTH = 200;

    public final GuiPanel contentPanel = new GuiPanel(this).setBackgroundColor(Colors.DARK_TRANSPARENT);
    public final GuiLabel spectateLabel = new GuiLabel(contentPanel)
            .setI18nText("replaymod.gui.playeroverview.spectate");
    public final GuiLabel visibleLabel = new GuiLabel(contentPanel)
            .setI18nText("replaymod.gui.playeroverview.visible");
    public final GuiVerticalList playersScrollable = new GuiVerticalList(contentPanel)
            .setDrawSlider(true).setDrawShadow(true);
    public final GuiCheckbox saveCheckbox = new GuiCheckbox(contentPanel)
            .setTooltip(new GuiTooltip().setI18nText("replaymod.gui.playeroverview.remembersettings.description"))
            .setI18nLabel("replaymod.gui.playeroverview.remembersettings");
    public final GuiCheckbox checkAll = new GuiCheckbox(contentPanel) {
        @Override
        public void onClick() {
            playersScrollable.invokeAll(IGuiCheckbox.class, e -> e.setChecked(true));
        }
    }.setLabel("").setChecked(true).setTooltip(new GuiTooltip().setI18nText("replaymod.gui.playeroverview.showall"));
    public final GuiCheckbox uncheckAll = new GuiCheckbox(contentPanel) {
        @Override
        public void onClick() {
            playersScrollable.invokeAll(IGuiCheckbox.class, e -> e.setChecked(false));
        }
    }.setLabel("").setChecked(false).setTooltip(new GuiTooltip().setI18nText("replaymod.gui.playeroverview.hideall"));

    {
        setBackground(Background.NONE);
        setTitle(new GuiLabel().setI18nText("replaymod.input.playeroverview"));
        setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                size(contentPanel, ENTRY_WIDTH + 30, height - 40);
                pos(contentPanel, width / 2 - width(contentPanel) / 2, 20);
            }
        });
        contentPanel.setLayout(new CustomLayout<GuiPanel>() {
            @Override
            protected void layout(GuiPanel container, int width, int height) {
                pos(spectateLabel, 10, 10);
                pos(visibleLabel, width - 10 - width(visibleLabel), 10);
                pos(playersScrollable, 10, y(spectateLabel) + height(spectateLabel) + 5);
                size(playersScrollable, width - 10 - 5, height - 15 - height(saveCheckbox) - y(playersScrollable));
                pos(saveCheckbox, 10, height - 10 - height(saveCheckbox));
                pos(uncheckAll, width - width(uncheckAll) - 8, height - height(uncheckAll) - 10);
                pos(checkAll, x(uncheckAll) - 3 - width(checkAll), y(uncheckAll));
            }
        });
    }

    private final PlayerOverview extra;

    public PlayerOverviewGui(final PlayerOverview extra, List<Player> players) {
        this.extra = extra;

        Collections.sort(players, new PlayerComparator()); // Sort by name, spectators last
        for (final Player p : players) {
            final ResourceLocation texture = Utils.getResourceLocationForPlayerUUID(p.getUUID());
            final GuiClickable panel = new GuiClickable().setLayout(new HorizontalLayout().setSpacing(2)).addElements(
                    new HorizontalLayout.Data(0.5), new GuiImage() {
                        @Override
                        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
                            renderer.bindTexture(texture);
                            renderer.drawTexturedRect(0, 0, 8, 8, 16, 16, 8, 8, 64, 64);
                            if (p.isModelPartShown(PlayerModelPart.HAT)) {
                                renderer.drawTexturedRect(0, 0, 40, 8, size.getWidth(), size.getHeight(), 8, 8, 64, 64);
                            }
                        }
                    }.setSize(16, 16),
                    new GuiLabel().setText(
                            p.getName().getString()
                    ).setColor(isSpectator(p) ? Colors.DKGREY : Colors.WHITE)
            ).onClick(new Runnable() {
                @Override
                public void run() {
                    ReplayModReplay.instance.getReplayHandler().spectateEntity(p);
                }
            });
            final GuiCheckbox checkbox = new GuiCheckbox() {
                @Override
                public GuiCheckbox setChecked(boolean checked) {
                    extra.setHidden(p.getUUID(), !checked);
                    return super.setChecked(checked);
                }
            }.setChecked(!extra.isHidden(p.getUUID()));
            new GuiPanel(playersScrollable.getListPanel()).setLayout(new CustomLayout<GuiPanel>() {
                @Override
                protected void layout(GuiPanel container, int width, int height) {
                    pos(panel, 5, 0);
                    pos(checkbox, width - width(checkbox) - 5, height / 2 - height(checkbox) / 2);
                }

                @Override
                public ReadableDimension calcMinSize(GuiContainer<?> container) {
                    return new Dimension(ENTRY_WIDTH, panel.getMinSize().getHeight());
                }
            }).addElements(null, panel, checkbox);
        }
        saveCheckbox.setChecked(extra.isSavingEnabled()).onClick(new Runnable() {
            @Override
            public void run() {
                extra.setSavingEnabled(saveCheckbox.isChecked());
            }
        });

        ReplayModReplay.instance.getReplayHandler().getOverlay().setVisible(false);
    }

    @Override
    public void close() {
        ReplayModReplay.instance.getReplayHandler().getOverlay().setVisible(true);
        extra.saveHiddenPlayers();
    }

    private static boolean isSpectator(Player e) {
        return e.isInvisible() && e.getEffect(MobEffects.INVISIBILITY) == null;
    }

    private static final class PlayerComparator implements Comparator<Player> {
        @Override
        public int compare(Player o1, Player o2) {
            if (isSpectator(o1) && !isSpectator(o2)) return 1;
            if (isSpectator(o2) && !isSpectator(o1)) return -1;
            return o1.getName().getString().compareToIgnoreCase(o2.getName().getString());
        }
    }
}

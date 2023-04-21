package com.replaymod.replay.handler;

import static com.replaymod.core.versions.MCVer.findButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.replaymod.core.gui.GuiReplayButton;
import com.replaymod.core.versions.MCVer;
import com.replaymod.gui.container.GuiScreen;
import com.replaymod.gui.container.VanillaGuiScreen;
import com.replaymod.gui.element.GuiElement;
import com.replaymod.gui.element.GuiTooltip;
import com.replaymod.gui.layout.CustomLayout;
import com.replaymod.gui.utils.EventRegistrations;
import com.replaymod.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replay.gui.screen.GuiReplayViewer;

import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


public class GuiHandler extends EventRegistrations {

	private final ReplayModReplay mod;

	public GuiHandler(ReplayModReplay mod) {
		on(InitScreenCallback.EVENT, this::injectIntoIngameMenu);
		on(InitScreenCallback.EVENT, (screen, buttons) -> ensureReplayStopped(screen));
		on(InitScreenCallback.EVENT, this::injectIntoMainMenu);
		this.mod = mod;
	}

	private void injectIntoIngameMenu(Screen guiScreen, Collection<AbstractWidget> buttonList) {
		if (!(guiScreen instanceof PauseScreen))
			return;
		if (this.mod.getReplayHandler() != null) {
			this.mod.getReplayHandler().getReplaySender().setReplaySpeed(0.0D);
			MutableComponent mutableText2 = Component.translatable("menu.disconnect");
			MutableComponent mutableText3 = Component.translatable("gui.advancements");
			MutableComponent mutableText4 = Component.translatable("gui.stats");
			MutableComponent mutableText5 = Component.translatable("menu.shareToLan");
			AbstractWidget achievements = null, stats = null;
			for (AbstractWidget b : new ArrayList<>(buttonList)) {
				boolean remove = false;
				Component id = b.getMessage();
				if (id == null)
					continue;
				if (id.equals(mutableText2)) {
					remove = true;
					MCVer.addButton(guiScreen, new InjectedButton(guiScreen, 17890235, b.x, b.y, b.getWidth(), b.getHeight(), "replaymod.gui.exit", this::onButton));
				} else if (id.equals(mutableText3)) {
					remove = true;
					achievements = b;
				} else if (id.equals(mutableText4)) {
					remove = true;
					stats = b;
				} else if (id.equals(mutableText5)) {
					remove = true;
				}
				if (remove) {
					b.x = -1000;
					b.y = -1000;
				}
			}
			if (achievements != null && stats != null)
				moveAllButtonsInRect(buttonList, achievements.x, stats.x + stats.getWidth(), achievements.y, 2147483647, -24);
		}
	}

	private void moveAllButtonsInRect(Collection<AbstractWidget> buttons, int xStart, int xEnd, int yStart, int yEnd,
			int moveBy) {
		buttons.stream().filter(button -> (button.x <= xEnd && button.x + button.getWidth() >= xStart))
				.filter(button -> (button.y <= yEnd && button.y + button.getHeight() >= yStart))
				.forEach(button -> button.y += moveBy);
	}

	private void ensureReplayStopped(Screen guiScreen) {
		if (!(guiScreen instanceof TitleScreen)
				&& !(guiScreen instanceof JoinMultiplayerScreen))
			return;
		if (this.mod.getReplayHandler() != null)
			try {
				this.mod.getReplayHandler().endReplay();
			} catch (IOException e) {
				ReplayModReplay.LOGGER.error("Trying to stop broken replay: ", e);
			} finally {
				if (this.mod.getReplayHandler() != null)
					this.mod.forcefullyStopReplay();
			}
	}

	private void injectIntoMainMenu(final Screen guiScreen, final Collection<AbstractWidget> buttonList) {
		if (!(guiScreen instanceof TitleScreen))
			return;
		boolean isCustomMainMenuMod = guiScreen.getClass().getName().endsWith("custommainmenu.gui.GuiFakeMain");
		final MainMenuButtonPosition buttonPosition = MainMenuButtonPosition.valueOf(this.mod.getCore()
				.getSettingsRegistry().get(Setting.MAIN_MENU_BUTTON));
		if (buttonPosition != MainMenuButtonPosition.BIG && !isCustomMainMenuMod) {
			VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(guiScreen);
			final GuiReplayButton replayButton = new GuiReplayButton();
			(replayButton.onClick(() -> (new GuiReplayViewer(this.mod)).display())).setTooltip(
					(new GuiTooltip()).setI18nText("replaymod.gui.replayviewer", new Object[0]));
			(vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
				private Point pos;

				protected void layout(GuiScreen container, int width, int height) {
					if (this.pos == null)
						this.pos = GuiHandler.this.determineButtonPos(buttonPosition, guiScreen, buttonList);
					size(replayButton, 20, 20);
					pos(replayButton, this.pos.getX(), this.pos.getY());
				}
			})).addElements(null, new GuiElement[] { replayButton });
			return;
		}
		int x = guiScreen.width / 2 - 100;
		int y = (((MCVer.findButton(buttonList, "menu.online", 14).map(Optional::of)
				.orElse(MCVer.findButton(buttonList, "menu.multiplayer", 2))).map(it -> Integer.valueOf(it.y)))
				.orElse(Integer.valueOf(guiScreen.height / 4 + 10 + 96))).intValue();
		moveAllButtonsInRect(buttonList, x, x + 200, -2147483648, y, -24);
		InjectedButton button = new InjectedButton(guiScreen, 17890234, x, y, 200, 20, "replaymod.gui.replayviewer",
				this::onButton);
		MCVer.addButton(guiScreen, button);
	}

    private Point determineButtonPos(MainMenuButtonPosition buttonPosition, Screen guiScreen, Collection<AbstractWidget> buttonList) {
        Point topRight = new Point(guiScreen.width - 20 - 5, 5);

        if (buttonPosition == MainMenuButtonPosition.TOP_LEFT) {
            return new Point(5, 5);
        } else if (buttonPosition == MainMenuButtonPosition.TOP_RIGHT) {
            return topRight;
        } else if (buttonPosition == MainMenuButtonPosition.DEFAULT) {
            return Stream.of(
                    findButton(buttonList, "menu.singleplayer", 1),
                    findButton(buttonList, "menu.multiplayer", 2),
                    findButton(buttonList, "menu.online", 14),
                    findButton(buttonList, "modmenu.title", 6)
            )
                    // skip buttons which do not exist
                    .flatMap(it -> it.map(Stream::of).orElseGet(Stream::empty))
                    // skip buttons which already have something next to them
                    .filter(it -> buttonList.stream().noneMatch(button ->
                            button.x <= it.x + it.getWidth() + 4 + 20
                                    && button.y <= it.y + it.getHeight()
                                    && button.x + button.getWidth() >= it.x + it.getWidth() + 4
                                    && button.y + button.getHeight() >= it.y
                    ))
                    // then take the bottom-most and if there's two, the right-most
                    .max(Comparator.<AbstractWidget>comparingInt(it -> it.y).thenComparingInt(it -> it.x))
                    // and place ourselves next to it
                    .map(it -> new Point(it.x + it.getWidth() + 4, it.y))
                    // if all fails, just go with TOP_RIGHT
                    .orElse(topRight);
        } else {
            return Optional.of(buttonList).flatMap(buttons -> {
                switch (buttonPosition) {
                    case LEFT_OF_SINGLEPLAYER:
                    case RIGHT_OF_SINGLEPLAYER:
                        return findButton(buttons, "menu.singleplayer", 1);
                    case LEFT_OF_MULTIPLAYER:
                    case RIGHT_OF_MULTIPLAYER:
                        return findButton(buttons, "menu.multiplayer", 2);
                    case LEFT_OF_REALMS:
                    case RIGHT_OF_REALMS:
                        return findButton(buttons, "menu.online", 14);
                    case LEFT_OF_MODS:
                    case RIGHT_OF_MODS:
                        return findButton(buttons, "modmenu.title", 6);
                }
                throw new RuntimeException();
            }).map(button -> {
                switch (buttonPosition) {
                    case LEFT_OF_SINGLEPLAYER:
                    case LEFT_OF_MULTIPLAYER:
                    case LEFT_OF_REALMS:
                    case LEFT_OF_MODS:
                        return new Point(button.x - 4 - 20, button.y);
                    case RIGHT_OF_MODS:
                    case RIGHT_OF_SINGLEPLAYER:
                    case RIGHT_OF_MULTIPLAYER:
                    case RIGHT_OF_REALMS:
                        return new Point(button.x + button.getWidth() + 4, button.y);
                }
                throw new RuntimeException();
            }).orElse(topRight);
        }
    }

	private void onButton(InjectedButton button) {
		Screen guiScreen = button.guiScreen;
		if (!button.active)
			return;
		if (guiScreen instanceof TitleScreen && button.id == 17890234)
		{
			(new GuiReplayViewer(this.mod)).display();
		}
		if (guiScreen instanceof PauseScreen && this.mod.getReplayHandler() != null && button.id == 17890235) {
			button.active = false;
			try {
				this.mod.getReplayHandler().endReplay();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static class InjectedButton extends Button {
		public final Screen guiScreen;

		public final int id;

		private Consumer<InjectedButton> onClick;

		public InjectedButton(Screen guiScreen, int buttonId, int x, int y, int width, int height, String buttonText,
				Consumer<InjectedButton> onClick) {
			super(x, y, width, height, Component.translatable(buttonText), self -> onClick.accept((InjectedButton) self));
			this.guiScreen = guiScreen;
			this.id = buttonId;
			this.onClick = onClick;
		}
	}

	public enum MainMenuButtonPosition {
		BIG, DEFAULT, TOP_LEFT, TOP_RIGHT, LEFT_OF_SINGLEPLAYER, RIGHT_OF_SINGLEPLAYER, LEFT_OF_MULTIPLAYER,
		RIGHT_OF_MULTIPLAYER, LEFT_OF_REALMS, RIGHT_OF_REALMS, LEFT_OF_MODS, RIGHT_OF_MODS;
	}
}

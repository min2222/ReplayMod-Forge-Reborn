package com.replaymod.replay.handler;

import static com.replaymod.core.versions.MCVer.addButton;
import static com.replaymod.core.versions.MCVer.findButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.replaymod.core.gui.GuiReplayButton;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replay.gui.screen.GuiReplayViewer;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

public class GuiHandler extends EventRegistrations {
	private static final int BUTTON_REPLAY_VIEWER = 17890234;
	private static final int BUTTON_EXIT_REPLAY = 17890235;
	private final ReplayModReplay mod;

	public GuiHandler(ReplayModReplay mod) {
		this.on(InitScreenCallback.EVENT, this::injectIntoIngameMenu);
		this.on(InitScreenCallback.EVENT, (screen, buttons) -> {
			this.ensureReplayStopped(screen);
		});
		this.on(InitScreenCallback.EVENT, this::injectIntoMainMenu);
		this.mod = mod;
	}

	private void injectIntoIngameMenu(Screen guiScreen, Collection<AbstractWidget> buttonList) {
		if (guiScreen instanceof PauseScreen) {
			if (this.mod.getReplayHandler() != null) {
				this.mod.getReplayHandler().getReplaySender().setReplaySpeed(0.0D);
				Component BUTTON_OPTIONS = Component.translatable("menu.options");
				Component BUTTON_EXIT_SERVER = Component.translatable("menu.disconnect");
				Component BUTTON_ADVANCEMENTS = Component.translatable("gui.advancements");
				Component BUTTON_STATS = Component.translatable("gui.stats");
				Component BUTTON_OPEN_TO_LAN = Component.translatable("menu.shareToLan");
				AbstractWidget achievements = null;
				AbstractWidget stats = null;
				Iterator var10 = (new ArrayList(buttonList)).iterator();

				while (var10.hasNext()) {
					AbstractWidget b = (AbstractWidget) var10.next();
					boolean remove = false;
					Component id = b.getMessage();
					if (id != null) {
						if (id.equals(BUTTON_EXIT_SERVER)) {
							remove = true;
							MCVer.addButton(guiScreen, new GuiHandler.InjectedButton(guiScreen, 17890235, b.x, b.y,
									b.getWidth(), b.getHeight(), "replaymod.gui.exit", (String) null, this::onButton));
						} else if (id.equals(BUTTON_ADVANCEMENTS)) {
							remove = true;
							achievements = b;
						} else if (id.equals(BUTTON_STATS)) {
							remove = true;
							stats = b;
						} else if (id.equals(BUTTON_OPEN_TO_LAN)) {
							remove = true;
						}

						if (remove) {
							b.x = -1000;
							b.y = -1000;
						}
					}
				}

				if (achievements != null && stats != null) {
					this.moveAllButtonsInRect(buttonList, achievements.x, stats.x + stats.getWidth(), achievements.y,
							Integer.MAX_VALUE, -24);
				}
			}

		}
	}

	private void moveAllButtonsInRect(Collection<AbstractWidget> buttons, int xStart, int xEnd, int yStart, int yEnd,
			int moveBy) {
		buttons.stream().filter((button) -> {
			return button.x <= xEnd && button.x + button.getWidth() >= xStart;
		}).filter((button) -> {
			return button.y <= yEnd && button.y + button.getHeight() >= yStart;
		}).forEach((button) -> {
			button.y += moveBy;
		});
	}

	private void ensureReplayStopped(Screen guiScreen) {
		if (guiScreen instanceof TitleScreen || guiScreen instanceof JoinMultiplayerScreen) {
			if (this.mod.getReplayHandler() != null) {
				try {
					this.mod.getReplayHandler().endReplay();
				} catch (IOException var6) {
					ReplayModReplay.LOGGER.error("Trying to stop broken replay: ", var6);
				} finally {
					if (this.mod.getReplayHandler() != null) {
						this.mod.forcefullyStopReplay();
					}

				}
			}

		}
	}

	private void injectIntoMainMenu(Screen guiScreen, Collection<AbstractWidget> buttonList) {
		if (guiScreen instanceof TitleScreen) {
			this.legacyInjectIntoMainMenu(guiScreen, buttonList);
		}
	}

	private void legacyInjectIntoMainMenu(Screen guiScreen, Collection<AbstractWidget> buttonList) {
		boolean isCustomMainMenuMod = guiScreen.getClass().getName().endsWith("custommainmenu.gui.GuiFakeMain");

		MainMenuButtonPosition buttonPosition = MainMenuButtonPosition
				.valueOf(mod.getCore().getSettingsRegistry().get(Setting.MAIN_MENU_BUTTON));
		if (buttonPosition != MainMenuButtonPosition.BIG && !isCustomMainMenuMod) {
			VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(guiScreen);

			GuiReplayButton replayButton = new GuiReplayButton();
			replayButton.onClick(() -> new GuiReplayViewer(mod).display())
					.setTooltip(new GuiTooltip().setI18nText("replaymod.gui.replayviewer"));

			vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
				private Point pos;

				@Override
				protected void layout(GuiScreen container, int width, int height) {
					if (pos == null) {
						// Delaying computation so we can take into account buttons
						// added after our callback.
						pos = determineButtonPos(buttonPosition, guiScreen, buttonList);
					}
					size(replayButton, 20, 20);
					pos(replayButton, pos.getX(), pos.getY());
				}
			}).addElements(null, replayButton);
			return;
		}

		int x = guiScreen.width / 2 - 100;
		// We want to position our button below the realms button
		int y = findButton(buttonList, "menu.online", 14).map(Optional::of)
				// or, if someone removed the realms button, we'll alternatively take the
				// multiplayer one
				.orElse(findButton(buttonList, "menu.multiplayer", 2))
				// if we found some button, put our button at its position (we'll move it out of
				// the way shortly)
				.map(it -> it.y)
				// and if we can't even find that one, then just guess
				.orElse(guiScreen.height / 4 + 10 + 4 * 24);

		// Move all buttons above or at our one upwards
		moveAllButtonsInRect(buttonList, x, x + 200, Integer.MIN_VALUE, y, -24);

		// Add our button
		InjectedButton button = new InjectedButton(guiScreen, BUTTON_REPLAY_VIEWER, x, y, 200, 20,
				"replaymod.gui.replayviewer", null, this::onButton);
		// #if FABRIC<=0
		// $$ if (isCustomMainMenuMod) {
		// $$ // CustomMainMenu uses a different list in the event than in its Fake gui
		// $$ buttonList.add(button);
		// $$ return;
		// $$ }
		// #endif
		addButton(guiScreen, button);
	}

	private Point determineButtonPos(MainMenuButtonPosition buttonPosition, Screen guiScreen,
			Collection<AbstractWidget> buttonList) {
		Point topRight = new Point(guiScreen.width - 20 - 5, 5);

		if (buttonPosition == MainMenuButtonPosition.TOP_LEFT) {
			return new Point(5, 5);
		} else if (buttonPosition == MainMenuButtonPosition.TOP_RIGHT) {
			return topRight;
		} else if (buttonPosition == MainMenuButtonPosition.DEFAULT) {
			return Stream
					.of(findButton(buttonList, "menu.singleplayer", 1), findButton(buttonList, "menu.multiplayer", 2),
							findButton(buttonList, "menu.online", 14), findButton(buttonList, "modmenu.title", 6))
					// skip buttons which do not exist
					.flatMap(it -> it.map(Stream::of).orElseGet(Stream::empty))
					// skip buttons which already have something next to them
					.filter(it -> buttonList.stream()
							.noneMatch(button -> button.x <= it.x + it.getWidth() + 4 + 20
									&& button.y <= it.y + it.getHeight()
									&& button.x + button.getWidth() >= it.x + it.getWidth() + 4
									&& button.y + button.getHeight() >= it.y))
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

	private int determineButtonIndex(Collection<AbstractWidget> buttons, AbstractWidget button) {
		AbstractWidget best = null;
		int bestIndex = -1;

		int index = 0;
		for (AbstractWidget other : buttons) {
			if (other.y > button.y || other.y == button.y && other.x > button.x) {
				index++;
				continue;
			}

			if (best == null || other.y > best.y || other.y == best.y && other.x > best.x) {
				best = other;
				bestIndex = index + 1;
			}

			index++;
		}
		return bestIndex;
	}

	private void onButton(GuiHandler.InjectedButton button) {
		Screen guiScreen = button.guiScreen;
		if (button.active) {
			if (guiScreen instanceof TitleScreen && button.id == 17890234) {
				(new GuiReplayViewer(this.mod)).display();
			}

			if (guiScreen instanceof PauseScreen && this.mod.getReplayHandler() != null && button.id == 17890235) {
				button.active = false;

				try {
					this.mod.getReplayHandler().endReplay();
				} catch (IOException var4) {
					var4.printStackTrace();
				}
			}

		}
	}

	public static class InjectedButton extends Button {
		public final Screen guiScreen;
		public final int id;
		private Consumer<GuiHandler.InjectedButton> onClick;

		public InjectedButton(Screen guiScreen, int buttonId, int x, int y, int width, int height,
				String buttonComponent, String tooltip, Consumer<GuiHandler.InjectedButton> onClick) {
			super(x, y, width, height, Component.translatable(buttonComponent), (self) -> {
				onClick.accept((GuiHandler.InjectedButton) self);
			}, tooltip != null ? (button, matrices, mouseX, mouseY) -> {
				guiScreen.renderTooltip(matrices, Component.translatable(tooltip), mouseX, mouseY);
			} : NO_TOOLTIP);
			this.guiScreen = guiScreen;
			this.id = buttonId;
			this.onClick = onClick;
		}
	}

	public static enum MainMenuButtonPosition {
		BIG, DEFAULT, TOP_LEFT, TOP_RIGHT, LEFT_OF_SINGLEPLAYER, RIGHT_OF_SINGLEPLAYER, LEFT_OF_MULTIPLAYER,
		RIGHT_OF_MULTIPLAYER, LEFT_OF_REALMS, RIGHT_OF_REALMS, LEFT_OF_MODS, RIGHT_OF_MODS;
	}
}

package com.replaymod.replay.gui.screen;

import static com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer.getFontRenderer;
import static com.replaymod.replay.ReplayModReplay.LOGGER;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.gui.GuiReplaySettings;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.core.versions.MCVer.Keyboard;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiImage;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.AbstractGuiResourceLoadingList;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiYesNoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import com.replaymod.render.gui.GuiRenderQueue;
import com.replaymod.render.rendering.VideoRenderer;
import com.replaymod.render.utils.RenderJob;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;

import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiReplayViewer extends GuiScreen {
	private final ReplayModReplay mod;
	public final GuiReplayViewer.GuiReplayList list = (GuiReplayViewer.GuiReplayList) ((GuiReplayViewer.GuiReplayList) (new GuiReplayViewer.GuiReplayList(
			this)).onSelectionChanged(this::updateButtons)).onSelectionDoubleClicked(() -> {
				if (this.loadButton.isEnabled()) {
					this.loadButton.onClick();
				}

			});
	public final GuiButton loadButton = (GuiButton) ((GuiButton) (new GuiButton()).onClick(new Runnable() {
		private boolean loading = false;

		public void run() {
			if (!this.loading) {
				this.loading = true;
				GuiReplayViewer.this.loadButton.setDisabled();
				List<GuiReplayViewer.GuiReplayEntry> selected = GuiReplayViewer.this.list.getSelected();
				if (selected.size() == 1) {
					File file = ((GuiReplayViewer.GuiReplayEntry) selected.get(0)).file;
					ReplayModReplay.LOGGER.info("Opening replay in viewer: " + file);

					try {
						GuiReplayViewer.this.mod.startReplay(file);
					} catch (IOException var4) {
						var4.printStackTrace();
					}
				} else {
					Iterator<Pair<File, List<RenderJob>>> replays = selected.stream().filter((it) -> {
						return !it.renderQueue.isEmpty();
					}).map((it) -> {
						return Pair.of(it.file, it.renderQueue);
					}).iterator();
					GuiRenderQueue.processMultipleReplays(GuiReplayViewer.this, GuiReplayViewer.this.mod, replays,
							() -> {
								this.loading = false;
								GuiReplayViewer.this.updateButtons();
								GuiReplayViewer.this.display();
							});
				}

			}
		}
	})).setSize(150, 20);
	public final GuiButton folderButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton())
			.onClick(new Runnable() {
				public void run() {
					try {
						File folder = GuiReplayViewer.this.mod.getCore().folders.getReplayFolder().toFile();
						MCVer.openFile(folder);
					} catch (IOException var2) {
						GuiReplayViewer.this.mod.getLogger().error("Cannot open file", var2);
					}

				}
			})).setSize(150, 20)).setI18nLabel("replaymod.gui.viewer.replayfolder", new Object[0]);
	public final GuiButton renameButton = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton())
			.onClick(new Runnable() {
				public void run() {
					Path path = ((GuiReplayViewer.GuiReplayEntry) GuiReplayViewer.this.list.getSelected().get(0)).file
							.toPath();
					String name = Utils.fileNameToReplayName(path.getFileName().toString());
					GuiTextField nameField = (GuiTextField) ((GuiTextField) ((GuiTextField) (new GuiTextField())
							.setSize(200, 20)).setFocused(true)).setText(name);
					GuiYesNoPopup popup = GuiYesNoPopup
							.open(GuiReplayViewer.this,
									((GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.viewer.rename.name",
											new Object[0])).setColor(Colors.BLACK),
									nameField)
							.setYesI18nLabel("replaymod.gui.rename").setNoI18nLabel("replaymod.gui.cancel");
					((VerticalLayout) popup.getInfo().getLayout()).setSpacing(7);
					nameField.onEnter(new Runnable() {
						@Override
						public void run() {
							if (popup.getYesButton().isEnabled()) {
								popup.getYesButton().onClick();
							}
						}
					}).onTextChanged((obj) -> {
						popup.getYesButton()
								.setEnabled(!nameField.getText().isEmpty() && Files.notExists(
										Utils.replayNameToPath(path.getParent(), nameField.getText()),
										new LinkOption[0]));
					});
					popup.onAccept(() -> {
						String newName = nameField.getText().trim();
						Path targetPath = Utils.replayNameToPath(path.getParent(), newName);

						try {
							Files.move(path, targetPath);
						} catch (IOException var6) {
							var6.printStackTrace();
							GuiReplayViewer.this.getMinecraft()
									.setScreen(new AlertScreen(GuiReplayViewer.this::display,
											Component.translatable("replaymod.gui.viewer.delete.failed1"),
											Component.translatable("replaymod.gui.viewer.delete.failed2")));
							return;
						}

						GuiReplayViewer.this.list.load();
					});
				}
			})).setSize(73, 20)).setI18nLabel("replaymod.gui.rename", new Object[0])).setDisabled();
	public final GuiButton deleteButton = new GuiButton().onClick(() -> {
		for (GuiReplayEntry entry : list.getSelected()) {
			String name = entry.name.getText();
			GuiYesNoPopup
					.open(GuiReplayViewer.this,
							new GuiLabel().setI18nText("replaymod.gui.viewer.delete.linea").setColor(Colors.BLACK),
							new GuiLabel().setI18nText("replaymod.gui.viewer.delete.lineb", name + ChatFormatting.RESET)
									.setColor(Colors.BLACK))
					.setYesI18nLabel("replaymod.gui.delete").setNoI18nLabel("replaymod.gui.cancel").onAccept(() -> {
						try {
							FileUtils.forceDelete(entry.file);
						} catch (IOException e) {
							e.printStackTrace();
						}
						list.load();
					});
		}
	}).setSize(73, 20).setI18nLabel("replaymod.gui.delete").setDisabled();
	public final GuiButton settingsButton;
	public final GuiButton cancelButton;
	public final List<GuiButton> replaySpecificButtons;
	public final GuiPanel editorButton;
	public final GuiPanel upperButtonPanel;
	public final GuiPanel lowerButtonPanel;
	public final GuiPanel buttonPanel;
	private static final GuiImage DEFAULT_THUMBNAIL;

	public GuiReplayViewer(ReplayModReplay mod) {
		this.settingsButton = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton(this))
				.setSize(20, 20)).setTexture(ReplayMod.TEXTURE, 256)).setSpriteUV(20, 0))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.settings", new Object[0]))).onClick(() -> {
					(new GuiReplaySettings(this.toMinecraft(), this.getMod().getCore().getSettingsRegistry()))
							.display();
				});
		this.cancelButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).onClick(new Runnable() {
			public void run() {
				GuiReplayViewer.this.getMinecraft().setScreen((Screen) null);
			}
		})).setSize(73, 20)).setI18nLabel("replaymod.gui.cancel", new Object[0]);
		this.replaySpecificButtons = new ArrayList();
		this.replaySpecificButtons.addAll(Arrays.asList(this.renameButton));
		this.editorButton = new GuiPanel();
		this.upperButtonPanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout()).setSpacing(5)))
				.addElements((LayoutData) null, new GuiElement[] { this.loadButton });
		this.lowerButtonPanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout()).setSpacing(5)))
				.addElements((LayoutData) null, new GuiElement[] { this.renameButton, this.deleteButton,
						this.editorButton, this.cancelButton });
		this.buttonPanel = (GuiPanel) ((GuiPanel) (new GuiPanel(this)).setLayout((new VerticalLayout()).setSpacing(5)))
				.addElements((LayoutData) null, new GuiElement[] { this.upperButtonPanel, this.lowerButtonPanel });
		this.mod = mod;

		try {
			this.list.setFolder(mod.getCore().folders.getReplayFolder().toFile());
		} catch (IOException var3) {
			throw new ReportedException(CrashReport.forThrowable(var3, "Getting replay folder"));
		}

		this.setTitle((GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.replayviewer", new Object[0]));
		this.setLayout(new CustomLayout<GuiScreen>() {
			protected void layout(GuiScreen container, int width, int height) {
				this.pos(GuiReplayViewer.this.buttonPanel, width / 2 - this.width(GuiReplayViewer.this.buttonPanel) / 2,
						height - 10 - this.height(GuiReplayViewer.this.buttonPanel));
				this.pos(GuiReplayViewer.this.list, 0, 30);
				this.size(GuiReplayViewer.this.list, width,
						this.y(GuiReplayViewer.this.buttonPanel) - 10 - this.y(GuiReplayViewer.this.list));
				this.pos(GuiReplayViewer.this.settingsButton,
						width - this.width(GuiReplayViewer.this.settingsButton) - 5, 5);
			}
		});
		this.updateButtons();
	}

	public ReplayModReplay getMod() {
		return this.mod;
	}

	private void updateButtons() {
		List<GuiReplayViewer.GuiReplayEntry> selected = this.list.getSelected();
		int count = selected.size();
		this.replaySpecificButtons.forEach((b) -> {
			b.setEnabled(count == 1);
		});
		this.deleteButton.setEnabled(count > 0);
		if (count > 1) {
			Set<RenderJob> jobs = (Set) selected.stream().flatMap((entry) -> {
				return entry.renderQueue.stream();
			}).collect(Collectors.toSet());
			String[] tooltipLines = (String[]) jobs.stream().map(RenderJob::getName).toArray((x$0) -> {
				return new String[x$0];
			});
			this.loadButton.setI18nLabel("replaymod.gui.viewer.bulkrender", new Object[] { jobs.size() });
			this.loadButton.setTooltip((new GuiTooltip()).setText(tooltipLines));
			this.loadButton.setEnabled(!jobs.isEmpty());
			String[] compatError = VideoRenderer.checkCompat(jobs.stream().map(RenderJob::getSettings));
			if (compatError != null) {
				((GuiButton) this.loadButton.setDisabled()).setTooltip((new GuiTooltip()).setText(compatError));
			}
		} else {
			this.loadButton.setI18nLabel("replaymod.gui.load", new Object[0]);
			this.loadButton.setTooltip((GuiElement) null);
			this.loadButton.setEnabled(count == 1 && !((GuiReplayViewer.GuiReplayEntry) selected.get(0)).incompatible);
		}

	}

	static {
		DEFAULT_THUMBNAIL = (GuiImage) (new GuiImage()).setTexture(Utils.DEFAULT_THUMBNAIL);
	}

	public static class GuiReplayList extends AbstractGuiResourceLoadingList<GuiReplayList, GuiReplayEntry>
			implements Typeable {
		private File folder = null;

		// Not actually a child of this element, we just use it for text manipulation
		private final GuiTextField filterTextField = new GuiTextField().setFocused(true);

		public GuiReplayList(GuiContainer container) {
			super(container);
		}

		{
			onLoad((Consumer<Supplier<GuiReplayEntry>> results) -> {
				File[] files = folder.listFiles((FileFilter) new SuffixFileFilter(".mcpr", IOCase.INSENSITIVE));
				if (files == null) {
					LOGGER.warn("Failed to list files in {}", folder);
					return;
				}
				Map<File, Long> lastModified = new HashMap<>();
				Arrays.sort(files, Comparator
						.<File>comparingLong(f -> lastModified.computeIfAbsent(f, File::lastModified)).reversed());
				for (final File file : files) {
					if (Thread.interrupted())
						break;
					try (ReplayFile replayFile = ReplayMod.instance.files.open(file.toPath())) {
						final Image thumb = Optional.ofNullable(replayFile.getThumbBytes().orNull()).flatMap(stream -> {
							try (InputStream in = stream) {
								return Optional.of(Image.read(in));
							} catch (IOException e) {
								e.printStackTrace();
								return Optional.empty();
							}
						}).orElse(null);
						final ReplayMetaData metaData = replayFile.getMetaData();
						List<RenderJob> renderQueue = RenderJob.readQueue(replayFile);

						if (metaData != null) {
							results.consume(() -> new GuiReplayEntry(file, metaData, thumb, renderQueue) {
								@Override
								public ReadableDimension calcMinSize() {
									if (isFiltered(this)) {
										return new Dimension(-4, -4);
									}
									return super.calcMinSize();
								}
							});
						}
					} catch (Exception e) {
						LOGGER.error("Could not load Replay File {}", file.getName(), e);
					}
				}
			}).setDrawShadow(true).setDrawSlider(true);
		}

		public void setFolder(File folder) {
			this.folder = folder;
		}

		private boolean isFiltered(GuiReplayEntry entry) {
			String filter = filterTextField.getText().toLowerCase();
			if (filter.isEmpty()) {
				return false;
			}
			return !entry.name.getText().toLowerCase().contains(filter);
		}

		@Override
		public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
				boolean shiftDown) {
			if (keyCode == Keyboard.KEY_F1) {
				SettingsRegistry reg = ReplayMod.instance.getSettingsRegistry();
				reg.set(Setting.SHOW_SERVER_IPS, !reg.get(Setting.SHOW_SERVER_IPS));
				reg.save();
				load();
			}

			boolean filterHasPriority = !filterTextField.getText().isEmpty();
			if (filterHasPriority && filterTextField.typeKey(mousePosition, keyCode, keyChar, ctrlDown, shiftDown)) {
				scrollY(0); // ensure we scroll to top if most entries are filtered
				return true;
			}

			if (super.typeKey(mousePosition, keyCode, keyChar, ctrlDown, shiftDown)) {
				return true;
			}

			if (!filterHasPriority && filterTextField.typeKey(mousePosition, keyCode, keyChar, ctrlDown, shiftDown)) {
				scrollY(0); // ensure we scroll to top if most entries are filtered
				return true;
			}

			return false;
		}

		@Override
		public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
			super.draw(renderer, size, renderInfo);

			String filter = filterTextField.getText();
			if (!filter.isEmpty()) {
				boolean anyMatches = getListPanel().calcMinSize().getHeight() > 0;

				Font fontRenderer = getFontRenderer();
				int filterTextWidth = fontRenderer.width(filter);
				int filterTextHeight = fontRenderer.lineHeight;
				renderer.drawRect(size.getWidth() - 3 - 2 - filterTextWidth - 2,
						size.getHeight() - 3 - 2 - filterTextHeight - 2, 2 + filterTextWidth + 2,
						2 + filterTextHeight + 2, Colors.WHITE);
				renderer.drawString(size.getWidth() - 3 - 2 - filterTextWidth,
						size.getHeight() - 3 - 2 - filterTextHeight, anyMatches ? Colors.BLACK : Colors.DARK_RED,
						filter);
			}
		}

		@Override
		protected GuiReplayList getThis() {
			return this;
		}
	}

	public static class GuiReplayEntry extends AbstractGuiContainer<GuiReplayViewer.GuiReplayEntry>
			implements Comparable<GuiReplayViewer.GuiReplayEntry> {
		public final File file;
		public final GuiLabel name = new GuiLabel();
		public final GuiLabel server;
		public final GuiLabel date;
		public final GuiPanel infoPanel;
		public final GuiLabel version;
		public final GuiImage thumbnail;
		public final GuiLabel duration;
		public final GuiPanel durationPanel;
		public final GuiImage renderQueueIcon;
		private final long dateMillis;
		private final boolean incompatible;
		private final List<RenderJob> renderQueue;

		public GuiReplayEntry(File file, ReplayMetaData metaData, Image thumbImage, List<RenderJob> renderQueue) {
			this.server = (GuiLabel) (new GuiLabel()).setColor(Colors.LIGHT_GRAY);
			this.date = (GuiLabel) (new GuiLabel()).setColor(Colors.LIGHT_GRAY);
			this.infoPanel = (GuiPanel) ((GuiPanel) (new GuiPanel(this))
					.setLayout((new VerticalLayout()).setSpacing(2)))
					.addElements((LayoutData) null, new GuiElement[] { this.name, this.server, this.date });
			this.version = (GuiLabel) (new GuiLabel(this)).setColor(Colors.RED);
			this.duration = new GuiLabel();
			this.durationPanel = new GuiPanel().setBackgroundColor(Colors.HALF_TRANSPARENT).addElements(null, duration)
					.setLayout(new CustomLayout<GuiPanel>() {
						@Override
						protected void layout(GuiPanel container, int width, int height) {
							pos(duration, 2, 2);
						}

						@Override
						public ReadableDimension calcMinSize(GuiContainer<?> container) {
							ReadableDimension dimension = duration.calcMinSize();
							return new Dimension(dimension.getWidth() + 2, dimension.getHeight() + 2);
						}
					});
			this.renderQueueIcon = (GuiImage) ((GuiImage) (new GuiImage()).setSize(10, 10))
					.setTexture(ReplayMod.TEXTURE, 40, 0, 20, 20);
			this.file = file;
			this.renderQueue = renderQueue;
			ChatFormatting var10001 = ChatFormatting.UNDERLINE;
			this.name.setText(var10001 + Utils.fileNameToReplayName(file.getName()));
			if (!StringUtils.isEmpty(metaData.getCustomServerName())) {
				this.server.setText(metaData.getCustomServerName());
			} else if (!StringUtils.isEmpty(metaData.getServerName())
					&& (Boolean) ReplayMod.instance.getSettingsRegistry().get(Setting.SHOW_SERVER_IPS)) {
				this.server.setText(metaData.getServerName());
			} else {
				((GuiLabel) this.server.setI18nText("replaymod.gui.iphidden", new Object[0])).setColor(Colors.DARK_RED);
			}

			this.incompatible = !ReplayMod.isCompatible(metaData.getFileFormatVersion(),
					metaData.getRawProtocolVersionOr0());
			if (this.incompatible) {
				this.version.setText("Minecraft " + metaData.getMcVersion());
			}

			this.dateMillis = metaData.getDate();
			this.date.setText((new SimpleDateFormat()).format(new Date(this.dateMillis)));
			if (thumbImage == null) {
				this.thumbnail = (GuiImage) (new GuiImage(GuiReplayViewer.DEFAULT_THUMBNAIL)).setSize(53, 30);
				this.addElements((LayoutData) null, new GuiElement[] { this.thumbnail });
			} else {
				this.thumbnail = (GuiImage) ((GuiImage) (new GuiImage(this)).setTexture(thumbImage)).setSize(53, 30);
			}

			this.duration.setText(Utils.convertSecondsToShortString(metaData.getDuration() / 1000));
			this.addElements((LayoutData) null, new GuiElement[] { this.durationPanel });
			if (!renderQueue.isEmpty()) {
				this.renderQueueIcon.setTooltip((new GuiTooltip())
						.setText((String[]) renderQueue.stream().map(RenderJob::getName).toArray((x$0) -> {
							return new String[x$0];
						})));
				this.addElements((LayoutData) null, new GuiElement[] { this.renderQueueIcon });
			}

			setLayout(new CustomLayout<GuiReplayEntry>() {
				@Override
				protected void layout(GuiReplayEntry container, int width, int height) {
					pos(thumbnail, 0, 0);
					x(durationPanel, width(thumbnail) - width(durationPanel));
					y(durationPanel, height(thumbnail) - height(durationPanel));

					pos(infoPanel, width(thumbnail) + 5, 0);
					pos(version, width - width(version), 0);

					if (renderQueueIcon.getContainer() != null) {
						pos(renderQueueIcon, width(thumbnail) - width(renderQueueIcon), 0);
					}
				}

				@Override
				public ReadableDimension calcMinSize(GuiContainer<?> container) {
					return new Dimension(300, thumbnail.getMinSize().getHeight());
				}
			});
		}

		protected GuiReplayViewer.GuiReplayEntry getThis() {
			return this;
		}

		public int compareTo(GuiReplayViewer.GuiReplayEntry o) {
			return Long.compare(o.dateMillis, this.dateMillis);
		}
	}

	public static class GuiSelectReplayPopup extends AbstractGuiPopup<GuiReplayViewer.GuiSelectReplayPopup> {
		private final SettableFuture<File> future = SettableFuture.create();
		private final GuiReplayViewer.GuiReplayList list;
		private final GuiButton acceptButton;
		private final GuiButton cancelButton;

		public static GuiReplayViewer.GuiSelectReplayPopup openGui(GuiContainer container, File folder) {
			GuiReplayViewer.GuiSelectReplayPopup popup = new GuiReplayViewer.GuiSelectReplayPopup(container, folder);
			popup.list.load();
			popup.open();
			return popup;
		}

		public GuiSelectReplayPopup(GuiContainer container, File folder) {
			super(container);
			this.list = new GuiReplayViewer.GuiReplayList(this.popup);
			this.acceptButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton(this.popup))
					.setI18nLabel("gui.done", new Object[0])).setSize(50, 20)).setDisabled();
			this.cancelButton = (GuiButton) ((GuiButton) (new GuiButton(this.popup)).setI18nLabel("gui.cancel",
					new Object[0])).setSize(50, 20);
			this.list.setFolder(folder);
			((GuiReplayViewer.GuiReplayList) this.list.onSelectionChanged(() -> {
				this.acceptButton.setEnabled(this.list.getSelected() != null);
			})).onSelectionDoubleClicked(() -> {
				this.close();
				this.future.set(((GuiReplayViewer.GuiReplayEntry) this.list.getSelected().get(0)).file);
			});
			this.acceptButton.onClick(() -> {
				this.future.set(((GuiReplayViewer.GuiReplayEntry) this.list.getSelected().get(0)).file);
				this.close();
			});
			this.cancelButton.onClick(() -> {
				this.future.set(null);
				this.close();
			});
			popup.setLayout(new CustomLayout<GuiPanel>() {
				@Override
				protected void layout(GuiPanel container, int width, int height) {
					pos(cancelButton, width - width(cancelButton), height - height(cancelButton));
					pos(acceptButton, x(cancelButton) - 5 - width(acceptButton), y(cancelButton));
					pos(list, 0, 5);
					size(list, width, height - height(cancelButton) - 10);
				}

				@Override
				public ReadableDimension calcMinSize(GuiContainer container) {
					return new Dimension(330, 200);
				}
			});
		}

		public SettableFuture<File> getFuture() {
			return this.future;
		}

		public GuiReplayViewer.GuiReplayList getList() {
			return this.list;
		}

		public GuiButton getAcceptButton() {
			return this.acceptButton;
		}

		public GuiButton getCancelButton() {
			return this.cancelButton;
		}

		protected GuiReplayViewer.GuiSelectReplayPopup getThis() {
			return this;
		}
	}
}

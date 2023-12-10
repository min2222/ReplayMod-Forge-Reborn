package com.replaymod.render.gui;

import static com.replaymod.render.ReplayModRender.LOGGER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Iterables;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Result;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiClickableContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiInfoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.render.FFmpegWriter;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.ReplayModRender;
import com.replaymod.render.rendering.VideoRenderer;
import com.replaymod.render.utils.RenderJob;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.ReplaySender;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.replay.ReplayFile;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;

public class GuiRenderQueue extends AbstractGuiPopup<GuiRenderQueue> implements Typeable {
	private final GuiLabel title;
	private final GuiVerticalList list;
	private final GuiButton addButton;
	private final GuiButton editButton;
	private final GuiButton removeButton;
	private final GuiButton renderButton;
	private final GuiButton closeButton;
	private final GuiPanel buttonPanel;
	private final AbstractGuiScreen<?> container;
	private final ReplayHandler replayHandler;
	private final Set<GuiRenderQueue.Entry> selectedEntries;
	private final Supplier<Result<Timeline, String[]>> timelineSupplier;
	private boolean opened;
	private final ReplayModRender mod;
	private final List<RenderJob> jobs;

	public GuiRenderQueue(AbstractGuiScreen<?> container, ReplayHandler replayHandler,
			Supplier<Result<Timeline, String[]>> timelineSupplier) {
		super(container);
		this.title = (GuiLabel) ((GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.renderqueue.title",
				new Object[0])).setColor(Colors.BLACK);
		this.list = (GuiVerticalList) ((GuiVerticalList) (new GuiVerticalList()).setDrawShadow(true))
				.setDrawSlider(true);
		this.addButton = (GuiButton) ((GuiButton) (new GuiButton()).setI18nLabel("replaymod.gui.renderqueue.add",
				new Object[0])).setSize(150, 20);
		this.editButton = (GuiButton) ((GuiButton) (new GuiButton()).setI18nLabel("replaymod.gui.edit", new Object[0]))
				.setSize(73, 20);
		this.removeButton = (GuiButton) ((GuiButton) (new GuiButton()).setI18nLabel("replaymod.gui.remove",
				new Object[0])).setSize(73, 20);
		this.renderButton = (GuiButton) (new GuiButton()).setSize(150, 20);
		this.closeButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).setI18nLabel("replaymod.gui.close",
				new Object[0])).setSize(150, 20)).onClick(this::close);
		this.buttonPanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new GridLayout()).setSpacingX(5).setSpacingY(5).setColumns(2)))
				.addElements((LayoutData) null, new GuiElement[] { this.addButton, this.renderButton,
						((GuiPanel) (new GuiPanel()).setLayout((new HorizontalLayout()).setSpacing(4))).addElements(
								(LayoutData) null, new GuiElement[] { this.editButton, this.removeButton }),
						this.closeButton });
		this.selectedEntries = new HashSet();
		((GuiPanel) this.popup.setLayout(new CustomLayout<GuiPanel>() {
			protected void layout(GuiPanel container, int width, int height) {
				this.pos(GuiRenderQueue.this.title, width / 2 - this.width(GuiRenderQueue.this.title) / 2, 0);
				this.pos(GuiRenderQueue.this.list, 0,
						this.y(GuiRenderQueue.this.title) + this.height(GuiRenderQueue.this.title) + 5);
				this.pos(GuiRenderQueue.this.buttonPanel, width / 2 - this.width(GuiRenderQueue.this.buttonPanel) / 2,
						height - this.height(GuiRenderQueue.this.buttonPanel));
				this.size(GuiRenderQueue.this.list, width,
						this.y(GuiRenderQueue.this.buttonPanel) - this.y(GuiRenderQueue.this.list) - 10);
			}

			public ReadableDimension calcMinSize(GuiContainer<?> container) {
				ReadableDimension screenSize = GuiRenderQueue.this.container.getMinSize();
				return new Dimension(screenSize.getWidth() - 40,
						screenSize.getHeight() - 20 - GuiRenderQueue.this.buttonPanel.getMinSize().getHeight()
								- GuiRenderQueue.this.title.getMinSize().getHeight());
			}
		})).addElements((LayoutData) null, new GuiElement[] { this.title, this.list, this.buttonPanel });
		this.mod = ReplayModRender.instance;
		this.jobs = this.mod.getRenderQueue();
		this.container = container;
		this.replayHandler = replayHandler;
		this.timelineSupplier = timelineSupplier;
		ReplayModRender.LOGGER.trace("Opening render queue popup");
		this.setBackgroundColor(Colors.DARK_TRANSPARENT);
		Iterator var4 = this.jobs.iterator();

		while (var4.hasNext()) {
			RenderJob renderJob = (RenderJob) var4.next();
			ReplayModRender.LOGGER.trace("Adding {} to job queue list", renderJob);
			this.list.getListPanel().addElements((LayoutData) null,
					new GuiElement[] { new GuiRenderQueue.Entry(renderJob) });
		}

		this.addButton.onClick(() -> {
			this.addButtonClicked().ifErr((lines) -> {
				GuiInfoPopup.open(container, (String[]) lines);
			});
		});
		this.editButton.onClick(() -> {
			GuiRenderQueue.Entry job = (GuiRenderQueue.Entry) this.selectedEntries.iterator().next();
			GuiRenderSettings gui = job.edit();
			gui.open();
		});
		this.removeButton.onClick(() -> {
			Iterator var1 = this.selectedEntries.iterator();

			while (var1.hasNext()) {
				GuiRenderQueue.Entry entry = (GuiRenderQueue.Entry) var1.next();
				ReplayModRender.LOGGER.trace("Remove button clicked for {}", entry.job);
				this.list.getListPanel().removeElement(entry);
				this.jobs.remove(entry.job);
			}

			this.selectedEntries.clear();
			this.updateButtons();
			this.mod.saveRenderQueue();
		});
		this.renderButton.onClick(() -> {
			ReplayModRender.LOGGER.trace("Render button clicked");
			List<RenderJob> renderQueue = new ArrayList();
			if (this.selectedEntries.isEmpty()) {
				renderQueue.addAll(this.jobs);
			} else {
				Set<RenderJob> selectedJobs = this.selectedEntries.stream().map((it) -> {
					return it.job;
				}).collect(Collectors.toSet());
				Iterator var5 = this.jobs.iterator();

				while (var5.hasNext()) {
					RenderJob job = (RenderJob) var5.next();
					if (selectedJobs.contains(job)) {
						renderQueue.add(job);
					}
				}
			}

			ReplayMod.instance.runLaterWithoutLock(() -> {
				processQueue(container, replayHandler, renderQueue, () -> {
				});
			});
		});
		this.updateButtons();
	}

	private static void processQueue(AbstractGuiScreen<?> container, ReplayHandler replayHandler,
			Iterable<RenderJob> queue, Runnable done) {
		Minecraft mc = MCVer.getMinecraft();

		// Close all GUIs (so settings in GuiRenderSettings are saved)
		mc.setScreen(null);
		// Start rendering
		int jobsDone = 0;
		for (RenderJob renderJob : queue) {
			LOGGER.info("Starting render job {}", renderJob);
			try {
				VideoRenderer videoRenderer = new VideoRenderer(renderJob.getSettings(), replayHandler,
						renderJob.getTimeline());
				videoRenderer.renderVideo();
			} catch (FFmpegWriter.NoFFmpegException e) {
				LOGGER.error("Rendering video:", e);
				mc.setScreen(new GuiNoFfmpeg(container::display).toMinecraft());
				return;
			} catch (FFmpegWriter.FFmpegStartupException e) {
				int jobsToSkip = jobsDone;
				GuiExportFailed.tryToRecover(e, newSettings -> {
					// Update current job with fixed ffmpeg arguments
					renderJob.setSettings(newSettings);
					// Restart queue, skipping the already completed jobs
					ReplayMod.instance.runLaterWithoutLock(
							() -> processQueue(container, replayHandler, Iterables.skip(queue, jobsToSkip), done));
				});
				return;
			} catch (Throwable t) {
				Utils.error(LOGGER, container, CrashReport.forThrowable(t, "Rendering video"), () -> {
				});
				container.display(); // Re-show the queue popup and the new error popup
				return;
			}
			jobsDone++;
		}
		done.run();
	}

	public static void processMultipleReplays(AbstractGuiScreen<?> container, ReplayModReplay mod,
			Iterator<Pair<File, List<RenderJob>>> queue, Runnable done) {
		if (!queue.hasNext()) {
			done.run();
			return;
		}
		Pair<File, List<RenderJob>> next = queue.next();

		LOGGER.info("Opening replay {} for {} render jobs", next.getKey(), next.getValue().size());
		ReplayHandler replayHandler;
		ReplayFile replayFile = null;
		try {
			replayFile = mod.getCore().files.open(next.getKey().toPath());
			replayHandler = mod.startReplay(replayFile, true, false);
		} catch (IOException e) {
			Utils.error(LOGGER, container, CrashReport.forThrowable(e, "Opening replay"), () -> {
			});
			container.display(); // Re-show the queue popup and the new error popup
			IOUtils.closeQuietly(replayFile);
			return;
		}
		if (replayHandler == null) {
			LOGGER.warn("Replay failed to open (missing mods?), skipping..");
			IOUtils.closeQuietly(replayFile);
			processMultipleReplays(container, mod, queue, done);
			return;
		}
		ReplaySender replaySender = replayHandler.getReplaySender();

		Minecraft mc = mod.getCore().getMinecraft();
		int jumpTo = 1000;
		while (mc.level == null && jumpTo < replayHandler.getReplayDuration()) {
			replaySender.sendPacketsTill(jumpTo);
			jumpTo += 1000;
		}
		if (mc.level == null) {
			LOGGER.warn("Replay failed to load world (corrupted?), skipping..");
			IOUtils.closeQuietly(replayFile);
			processMultipleReplays(container, mod, queue, done);
			return;
		}

		processQueue(container, replayHandler, next.getValue(), () -> {
			try {
				replayHandler.endReplay();
			} catch (IOException e) {
				Utils.error(LOGGER, container, CrashReport.forThrowable(e, "Closing replay"), () -> {
				});
				container.display(); // Re-show the queue popup and the new error popup
				return;
			}
			processMultipleReplays(container, mod, queue, done);
		});
	}

	private Result<GuiRenderSettings, String[]> addButtonClicked() {
		return timelineSupplier.get().mapOk(timeline -> {
			GuiRenderSettings popup = addJob(timeline);
			popup.open();
			return popup;
		});
	}

	public GuiRenderSettings addJob(Timeline timeline) {
		return new GuiRenderSettings(this.container, this.replayHandler, timeline) {
			{
				if (!GuiRenderQueue.this.jobs.isEmpty()) {
					this.buttonPanel.removeElement(this.renderButton);
				}

				this.queueButton.onClick(() -> {
					RenderSettings settings = this.save(false);
					RenderJob newJob = new RenderJob();
					newJob.setSettings(settings);
					newJob.setTimeline(timeline);
					ReplayModRender.LOGGER.trace("Adding new job: {}", newJob);
					GuiRenderQueue.this.jobs.add(newJob);
					GuiRenderQueue.this.list.getListPanel().addElements((LayoutData) null,
							new GuiElement[] { GuiRenderQueue.this.new Entry(newJob) });
					GuiRenderQueue.this.updateButtons();
					GuiRenderQueue.this.mod.saveRenderQueue();
					this.close();
					if (!GuiRenderQueue.this.opened) {
						GuiRenderQueue.this.open();
					}

				});
			}

			public void close() {
				super.close();
				if (!GuiRenderQueue.this.opened && GuiRenderQueue.this.jobs.isEmpty()) {
					GuiRenderQueue.this.close();
				}

			}
		};
	}

	public void open() {
		if (this.jobs.isEmpty() && this.timelineSupplier != null) {
			this.addButtonClicked().ifErr((lines) -> {
				GuiInfoPopup.open(this.container, (String[]) lines).onClosed(this::close);
			});
		} else {
			super.open();
			this.opened = true;
		}
	}

	protected void close() {
		if (this.opened) {
			super.close();
		}

		this.opened = false;
	}

	protected GuiRenderQueue getThis() {
		return this;
	}

	public void updateButtons() {
		int selected = this.selectedEntries.size();
		this.addButton.setEnabled(this.timelineSupplier != null);
		this.editButton.setEnabled(selected == 1);
		this.removeButton.setEnabled(selected >= 1);
		this.renderButton.setEnabled(this.jobs.size() > 0);
		this.renderButton.setI18nLabel("replaymod.gui.renderqueue.render" + (selected > 0 ? "selected" : "all"),
				new Object[0]);
		String[] compatError = VideoRenderer.checkCompat(this.jobs.stream().map(RenderJob::getSettings));
		if (compatError != null) {
			((GuiButton) this.renderButton.setDisabled()).setTooltip((new GuiTooltip()).setText(compatError));
		}

	}

	public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
			boolean shiftDown) {
		if (MCVer.Keyboard.hasControlDown() && keyCode == 65) {
			if (this.selectedEntries.size() < this.list.getListPanel().getChildren().size()) {
				Iterator var6 = this.list.getListPanel().getChildren().iterator();

				while (var6.hasNext()) {
					GuiElement<?> child = (GuiElement) var6.next();
					if (child instanceof GuiRenderQueue.Entry) {
						this.selectedEntries.add((GuiRenderQueue.Entry) child);
					}
				}
			} else {
				this.selectedEntries.clear();
			}

			this.updateButtons();
			return true;
		} else {
			return false;
		}
	}

	public class Entry extends AbstractGuiClickableContainer<GuiRenderQueue.Entry> {
		public final GuiLabel label = new GuiLabel(this);
		public final RenderJob job;

		public Entry(RenderJob job) {
			this.job = job;

			setLayout(new CustomLayout<Entry>() {
				@Override
				protected void layout(Entry container, int width, int height) {
					pos(label, 5, height / 2 - height(label) / 2);
				}

				@Override
				public ReadableDimension calcMinSize(GuiContainer<?> container) {
					return new Dimension(buttonPanel.calcMinSize().getWidth(), 16);
				}
			});
			label.setText(job.getName());
		}

		protected void onClick() {
			if (!MCVer.Keyboard.hasControlDown()) {
				GuiRenderQueue.this.selectedEntries.clear();
			}

			if (GuiRenderQueue.this.selectedEntries.contains(this)) {
				GuiRenderQueue.this.selectedEntries.remove(this);
			} else {
				GuiRenderQueue.this.selectedEntries.add(this);
			}

			GuiRenderQueue.this.updateButtons();
		}

		public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
			if (GuiRenderQueue.this.selectedEntries.contains(this)) {
				renderer.drawRect(0, 0, size.getWidth(), size.getHeight(), Colors.BLACK);
				renderer.drawRect(0, 0, 2, size.getHeight(), Colors.WHITE);
			}

			super.draw(renderer, size, renderInfo);
		}

		protected GuiRenderQueue.Entry getThis() {
			return this;
		}

		public GuiRenderSettings edit() {
			GuiRenderSettings gui = new GuiRenderSettings(GuiRenderQueue.this.container,
					GuiRenderQueue.this.replayHandler, this.job.getTimeline());
			gui.buttonPanel.removeElement(gui.renderButton);
			((GuiButton) gui.queueButton.setI18nLabel("replaymod.gui.done", new Object[0])).onClick(() -> {
				this.job.setSettings(gui.save(false));
				this.label.setText(this.job.getName());
				GuiRenderQueue.this.mod.saveRenderQueue();
				gui.close();
			});
			gui.load(this.job.getSettings());
			return gui;
		}
	}
}

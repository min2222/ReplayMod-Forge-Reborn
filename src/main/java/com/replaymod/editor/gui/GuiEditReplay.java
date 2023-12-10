package com.replaymod.editor.gui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.editor.ReplayModEditor;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiHorizontalScrollbar;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiProgressBar;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiTimelineTime;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.replay.gui.overlay.GuiMarkerTimeline;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.replay.ReplayFile;

import net.minecraft.CrashReport;

public class GuiEditReplay extends AbstractGuiPopup<GuiEditReplay> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Path inputPath;
	private final GuiEditReplay.EditTimeline timeline;
	private final GuiTimelineTime<GuiMarkerTimeline> timelineTime = new GuiTimelineTime();
	private final GuiHorizontalScrollbar scrollbar = (GuiHorizontalScrollbar) (new GuiHorizontalScrollbar())
			.setSize(300, 9);
	private final GuiButton zoomInButton;
	private final GuiButton zoomOutButton;
	private final GuiPanel zoomButtonPanel;
	private Set<Marker> markers;

	protected GuiEditReplay(GuiContainer container, Path inputPath) throws IOException {
		super(container);
		this.zoomInButton = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).setSize(9,
				9)).onClick(() -> {
					this.zoomTimeline(0.6666666666666666D);
				})).setTexture(ReplayMod.TEXTURE, 256)).setSpriteUV(40, 20))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.ingame.menu.zoomin", new Object[0]));
		this.zoomOutButton = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton())
				.setSize(9, 9)).onClick(() -> {
					this.zoomTimeline(1.5D);
				})).setTexture(ReplayMod.TEXTURE, 256)).setSpriteUV(40, 30))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.ingame.menu.zoomout", new Object[0]));
		this.zoomButtonPanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new VerticalLayout(VerticalLayout.Alignment.CENTER)).setSpacing(2)))
				.addElements((LayoutData) null, new GuiElement[] { this.zoomInButton, this.zoomOutButton });
		this.inputPath = inputPath;
		LOGGER.info("Opening replay in editor: " + inputPath);
		ReplayFile replayFile = ReplayMod.instance.files.open(inputPath);

		try {
			this.markers = (Set) replayFile.getMarkers().or(HashSet::new);
			this.timeline = new GuiEditReplay.EditTimeline(new HashSet(this.markers), (markers) -> {
				this.markers = markers;
			});
			GuiMarkerTimeline var10000 = (GuiMarkerTimeline) ((GuiMarkerTimeline) ((GuiMarkerTimeline) this.timeline
					.setSize(300, 20)).setMarkers()).setLength(replayFile.getMetaData().getDuration());
			GuiEditReplay.EditTimeline var10001 = this.timeline;
			Objects.requireNonNull(var10001);
			var10000.onClick(var10001::setCursorPosition);
		} catch (Throwable var12) {
			if (replayFile != null) {
				try {
					replayFile.close();
				} catch (Throwable var11) {
					var12.addSuppressed(var11);
				}
			}

			throw var12;
		}

		if (replayFile != null) {
			replayFile.close();
		}

		this.timelineTime.setTimeline(this.timeline);
		((GuiHorizontalScrollbar) this.scrollbar.onValueChanged(() -> {
			this.timeline.setOffset((int) (this.scrollbar.getPosition() * (double) this.timeline.getLength()));
			this.timeline.setZoom(this.scrollbar.getZoom());
		})).setZoom(1.0D);
		GuiPanel timelinePanel = (GuiPanel) ((GuiPanel) ((GuiPanel) (new GuiPanel()).setSize(300, 40))
				.setLayout(new CustomLayout<GuiPanel>() {
					protected void layout(GuiPanel container, int width, int height) {
						this.pos(GuiEditReplay.this.zoomButtonPanel,
								width - this.width(GuiEditReplay.this.zoomButtonPanel), 10);
						this.pos(GuiEditReplay.this.timelineTime, 0, 2);
						this.size(GuiEditReplay.this.timelineTime, this.x(GuiEditReplay.this.zoomButtonPanel), 8);
						this.pos(GuiEditReplay.this.timeline, 0,
								this.y(GuiEditReplay.this.timelineTime) + this.height(GuiEditReplay.this.timelineTime));
						this.size(GuiEditReplay.this.timeline, this.x(GuiEditReplay.this.zoomButtonPanel) - 2, 20);
						this.pos(GuiEditReplay.this.scrollbar, 0,
								this.y(GuiEditReplay.this.timeline) + this.height(GuiEditReplay.this.timeline) + 1);
						this.size(GuiEditReplay.this.scrollbar, this.x(GuiEditReplay.this.zoomButtonPanel) - 2, 9);
					}
				})).addElements((LayoutData) null,
						new GuiElement[] { this.timelineTime, this.timeline, this.scrollbar, this.zoomButtonPanel });
		GuiButton buttonAddSplit = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).setSize(100,
				20)).setI18nLabel("replaymod.gui.edit.split", new Object[0]))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.edit.split.tooltip", new Object[0])))
				.onClick(() -> {
					Marker marker = new Marker();
					marker.setTime(this.timeline.getCursorPosition());
					marker.setName("_RM_SPLIT");
					this.timeline.addMarker(marker);
				});
		GuiButton buttonInsertCut = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).setSize(100,
				20)).setI18nLabel("replaymod.gui.edit.cut.start", new Object[0]))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.edit.cut.tooltip", new Object[0])))
				.onClick(() -> {
					Marker marker = new Marker();
					marker.setTime(this.timeline.getCursorPosition());
					marker.setName("_RM_START_CUT");
					this.timeline.addMarker(marker);
				});
		GuiButton buttonEndCut = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).setSize(100, 20))
				.setI18nLabel("replaymod.gui.edit.cut.end", new Object[0]))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.edit.cut.tooltip", new Object[0])))
				.onClick(() -> {
					Marker marker = new Marker();
					marker.setTime(this.timeline.getCursorPosition());
					marker.setName("_RM_END_CUT");
					this.timeline.addMarker(marker);
				});
		GuiPanel controlPanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout()).setSpacing(4)))
				.addElements((LayoutData) null, new GuiElement[] { buttonAddSplit, buttonInsertCut, buttonEndCut });
		GuiButton applyButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton())
				.setI18nLabel("replaymod.gui.edit.apply", new Object[0])).setSize(150, 20)).onClick(this::apply);
		GuiButton closeButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton())
				.setI18nLabel("replaymod.gui.close", new Object[0])).setSize(150, 20)).onClick(() -> {
					this.close();
				});
		GuiPanel buttonPanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout()).setSpacing(8)))
				.addElements((LayoutData) null, new GuiElement[] { applyButton, closeButton });
		this.popup.setLayout((new VerticalLayout(VerticalLayout.Alignment.TOP)).setSpacing(10));
		this.popup.addElements(new VerticalLayout.Data(0.5D),
				new GuiElement[] { timelinePanel, controlPanel, buttonPanel });
	}

	private void zoomTimeline(double factor) {
		this.scrollbar.setZoom(this.scrollbar.getZoom() * factor);
	}

	private void apply() {
		GuiEditReplay.ProgressPopup progressPopup = new GuiEditReplay.ProgressPopup(this);
		(new Thread(() -> {
			try {
				ReplayFile replayFile = ReplayMod.instance.files.open(this.inputPath);

				try {
					replayFile.writeMarkers(this.markers);
					replayFile.save();
				} catch (Throwable var7) {
					if (replayFile != null) {
						try {
							replayFile.close();
						} catch (Throwable var6) {
							var7.addSuppressed(var6);
						}
					}

					throw var7;
				}

				if (replayFile != null) {
					replayFile.close();
				}
			} catch (IOException var8) {
				Utils.error(ReplayModEditor.LOGGER, this, CrashReport.forThrowable(var8, "Writing markers"), () -> {
					this.close();
				});
			}

			try {
				Path var10000 = this.inputPath;
				GuiProgressBar var10001 = progressPopup.progressBar;
				Objects.requireNonNull(var10001);
				MarkerProcessor.apply(var10000, var10001::setProgress);
				ReplayMod.instance.runLater(() -> {
					progressPopup.close();
					this.close();
				});
			} catch (Throwable var5) {
				var5.printStackTrace();
				CrashReport crashReport = CrashReport.forThrowable(var5, "Running marker processor");
				ReplayMod.instance.runLater(() -> {
					Utils.error(ReplayModEditor.LOGGER, this, crashReport, () -> {
						progressPopup.close();
						this.close();
					});
				});
			}

		})).start();
	}

	public void open() {
		super.open();
	}

	protected GuiEditReplay getThis() {
		return this;
	}

	private class EditTimeline extends GuiMarkerTimeline {
		EditTimeline(Set<Marker> markers, Consumer<Set<Marker>> saveMarkers) {
			super(markers, saveMarkers);
		}

		protected void drawMarkers(GuiRenderer renderer, ReadableDimension size) {
			this.drawCutQuads(renderer, size);
			super.drawMarkers(renderer, size);
		}

		protected void drawMarker(GuiRenderer renderer, ReadableDimension size, Marker marker, int markerX) {
			if ("_RM_SPLIT".equals(marker.getName())) {
				int height = size.getHeight() - 3 - 4 - 5 + 1;

				for (int y = 0; y < height; y += 3) {
					renderer.drawRect(markerX, 4 + y, 1, 2, Color.WHITE);
				}
			}

			super.drawMarker(renderer, size, marker, markerX);
		}

		private void drawCutQuads(GuiRenderer renderer, ReadableDimension size) {
			boolean inCut = false;
			int startTime = 0;
			Iterator var5 = ((List) this.markers.stream().sorted(Comparator.comparing(Marker::getTime))
					.collect(Collectors.toList())).iterator();

			while (true) {
				while (var5.hasNext()) {
					Marker marker = (Marker) var5.next();
					if ("_RM_START_CUT".equals(marker.getName()) && !inCut) {
						inCut = true;
						startTime = marker.getTime();
					} else if ("_RM_END_CUT".equals(marker.getName()) && inCut) {
						this.drawCutQuad(renderer, size, startTime, marker.getTime());
						inCut = false;
					}
				}

				if (inCut) {
					this.drawCutQuad(renderer, size, startTime, this.getLength());
				}

				return;
			}
		}

		private void drawCutQuad(GuiRenderer renderer, ReadableDimension size, int startFrameTime, int endFrameTime) {
			int visibleWidth = size.getWidth() - 4 - 4;
			int startTime = this.getOffset();
			int visibleTime = (int) (this.getZoom() * (double) this.getLength());
			int endTime = this.getOffset() + visibleTime;
			if (startFrameTime < endTime && endFrameTime > startTime) {
				double relativeStart = (double) (startFrameTime - startTime);
				double relativeEnd = (double) (endFrameTime - startTime);
				int startX = 4
						+ Math.max(0, (int) (relativeStart / (double) visibleTime * (double) visibleWidth) + 2 + 1);
				int endX = 4 + Math.min(visibleWidth,
						(int) (relativeEnd / (double) visibleTime * (double) visibleWidth) - 2);
				if (startX < endX) {
					renderer.drawRect(startX + 1, size.getHeight() - 3 - 5, endX - startX - 2, 3, Color.RED);
				}

			}
		}
	}

	private class ProgressPopup extends AbstractGuiPopup<GuiEditReplay.ProgressPopup> {
		private final GuiProgressBar progressBar;

		ProgressPopup(GuiContainer container) {
			super(container);
			this.progressBar = (GuiProgressBar) (new GuiProgressBar(this.popup)).setSize(300, 20);
			this.open();
		}

		public void close() {
			super.close();
		}

		protected GuiEditReplay.ProgressPopup getThis() {
			return this;
		}
	}
}

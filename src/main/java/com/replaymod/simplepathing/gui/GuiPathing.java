package com.replaymod.simplepathing.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Result;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.core.versions.MCVer.Keyboard;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiHorizontalScrollbar;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiProgressBar;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiTimelineTime;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiInfoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiYesNoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;
import com.replaymod.pathing.gui.GuiKeyframeRepository;
import com.replaymod.pathing.player.RealtimeTimelinePlayer;
import com.replaymod.pathing.properties.CameraProperties;
import com.replaymod.pathing.properties.SpectatorProperty;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.render.gui.GuiRenderQueue;
import com.replaymod.render.gui.GuiRenderSettings;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.camera.CameraEntity;
import com.replaymod.replay.gui.overlay.GuiReplayOverlay;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.util.EntityPositionTracker;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import com.replaymod.simplepathing.SPTimeline;
import com.replaymod.simplepathing.Setting;

import net.minecraft.CrashReport;
import net.minecraft.client.resources.language.I18n;

public class GuiPathing {
	private static final Logger logger = LogManager.getLogger();
	public final GuiButton playPauseButton;
	public final GuiButton renderButton;
	public final GuiButton positionKeyframeButton;
	public final GuiButton timeKeyframeButton;
	public final GuiKeyframeTimeline timeline;
	public final GuiHorizontalScrollbar scrollbar;
	public final GuiTimelineTime<GuiKeyframeTimeline> timelineTime;
	public final GuiButton zoomInButton;
	public final GuiButton zoomOutButton;
	public final GuiPanel zoomButtonPanel;
	public final GuiPanel timelinePanel;
	public final GuiPanel panel;
	private final ReplayMod core;
	private final ReplayModSimplePathing mod;
	private final ReplayHandler replayHandler;
	public final GuiReplayOverlay overlay;
	private final RealtimeTimelinePlayer player;
	private boolean errorShown;
	private EntityPositionTracker entityTracker;
	private Consumer<Double> entityTrackerLoadingProgress;
	private SettableFuture<Void> entityTrackerFuture;
	private int prevSpeed;
	private int prevTime;

	public GuiPathing(ReplayMod core, ReplayModSimplePathing mod, ReplayHandler replayHandler) {
		this.playPauseButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton() {
			public GuiElement getTooltip(RenderInfo renderInfo) {
				GuiTooltip tooltip = (GuiTooltip) super.getTooltip(renderInfo);
				if (tooltip != null) {
					if (GuiPathing.this.player.isActive()) {
						tooltip.setI18nText("replaymod.gui.ingame.menu.pausepath", new Object[0]);
					} else if (MCVer.Keyboard.isKeyDown(341)) {
						tooltip.setI18nText("replaymod.gui.ingame.menu.playpathfromstart", new Object[0]);
					} else {
						tooltip.setI18nText("replaymod.gui.ingame.menu.playpath", new Object[0]);
					}
				}

				return tooltip;
			}
		}).setSize(20, 20)).setTexture(ReplayMod.TEXTURE, 256)).setTooltip(new GuiTooltip());
		this.renderButton = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton())
				.onClick(new Runnable() {
					@Override
					public void run() {
						abortPathPlayback();
						GuiScreen screen = GuiRenderSettings.createBaseScreen();
						new GuiRenderQueue(screen, replayHandler, () -> preparePathsForPlayback(false)) {
							@Override
							protected void close() {
								super.close();
								getMinecraft().setScreen(null);
							}
						}.open();
						screen.display();
					}
				})).setSize(20, 20)).setTexture(ReplayMod.TEXTURE, 256)).setSpriteUV(40, 0))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.ingame.menu.renderpath", new Object[0]));
		this.positionKeyframeButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton() {
			public GuiElement getTooltip(RenderInfo renderInfo) {
				GuiTooltip tooltip = (GuiTooltip) super.getTooltip(renderInfo);
				if (tooltip != null) {
					String label;
					if (this.getSpriteUV().getY() == 40) {
						if (this.getSpriteUV().getX() == 0) {
							label = "replaymod.gui.ingame.menu.addposkeyframe";
						} else {
							label = "replaymod.gui.ingame.menu.addspeckeyframe";
						}
					} else if (this.getSpriteUV().getX() == 0) {
						label = "replaymod.gui.ingame.menu.removeposkeyframe";
					} else {
						label = "replaymod.gui.ingame.menu.removespeckeyframe";
					}

					String var10001 = I18n.get(label, new Object[0]);
					tooltip.setText(var10001 + " (" + GuiPathing.this.mod.keyPositionKeyframe.getBoundKey() + ")");
				}

				return tooltip;
			}
		}).setSize(20, 20)).setTexture(ReplayMod.TEXTURE, 256)).setTooltip(new GuiTooltip());
		this.timeKeyframeButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton() {
			public GuiElement getTooltip(RenderInfo renderInfo) {
				GuiTooltip tooltip = (GuiTooltip) super.getTooltip(renderInfo);
				if (tooltip != null) {
					String label;
					if (this.getSpriteUV().getY() == 80) {
						label = "replaymod.gui.ingame.menu.addtimekeyframe";
					} else {
						label = "replaymod.gui.ingame.menu.removetimekeyframe";
					}

					String var10001 = I18n.get(label, new Object[0]);
					tooltip.setText(var10001 + " (" + GuiPathing.this.mod.keyTimeKeyframe.getBoundKey() + ")");
				}

				return tooltip;
			}
		}).setSize(20, 20)).setTexture(ReplayMod.TEXTURE, 256)).setTooltip(new GuiTooltip());
		this.timeline = (GuiKeyframeTimeline) ((GuiKeyframeTimeline) (new GuiKeyframeTimeline(this) {
			public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
				if (GuiPathing.this.player.isActive()) {
					((GuiKeyframeTimeline) this.setCursorPosition((int) GuiPathing.this.player.getTimePassed()))
							.ensureCursorVisibleWithPadding();
				}

				super.draw(renderer, size, renderInfo);
			}
		}).setSize(Integer.MAX_VALUE, 20)).setMarkers();
		this.scrollbar = (GuiHorizontalScrollbar) (new GuiHorizontalScrollbar()).setSize(Integer.MAX_VALUE, 9);
		((GuiHorizontalScrollbar) this.scrollbar.onValueChanged(new Runnable() {
			public void run() {
				GuiPathing.this.timeline.setOffset((int) (GuiPathing.this.scrollbar.getPosition()
						* (double) GuiPathing.this.timeline.getLength()));
				GuiPathing.this.timeline.setZoom(GuiPathing.this.scrollbar.getZoom());
			}
		})).setZoom(0.1D);
		this.timelineTime = (GuiTimelineTime) (new GuiTimelineTime()).setTimeline(this.timeline);
		this.zoomInButton = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).setSize(9,
				9)).onClick(new Runnable() {
					public void run() {
						GuiPathing.this.zoomTimeline(0.6666666666666666D);
					}
				})).setTexture(ReplayMod.TEXTURE, 256)).setSpriteUV(40, 20))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.ingame.menu.zoomin", new Object[0]));
		this.zoomOutButton = (GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) ((GuiButton) (new GuiButton())
				.setSize(9, 9)).onClick(new Runnable() {
					public void run() {
						GuiPathing.this.zoomTimeline(1.5D);
					}
				})).setTexture(ReplayMod.TEXTURE, 256)).setSpriteUV(40, 30))
				.setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.ingame.menu.zoomout", new Object[0]));
		this.zoomButtonPanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new VerticalLayout(VerticalLayout.Alignment.CENTER)).setSpacing(2)))
				.addElements((LayoutData) null, new GuiElement[] { this.zoomInButton, this.zoomOutButton });
		this.timelinePanel = (GuiPanel) ((GuiPanel) ((GuiPanel) (new GuiPanel()).setSize(Integer.MAX_VALUE, 40))
				.setLayout(new CustomLayout<GuiPanel>() {
					protected void layout(GuiPanel container, int width, int height) {
						this.pos(GuiPathing.this.zoomButtonPanel, width - this.width(GuiPathing.this.zoomButtonPanel),
								10);
						this.pos(GuiPathing.this.timelineTime, 0, 2);
						this.size(GuiPathing.this.timelineTime, this.x(GuiPathing.this.zoomButtonPanel), 8);
						this.pos(GuiPathing.this.timeline, 0,
								this.y(GuiPathing.this.timelineTime) + this.height(GuiPathing.this.timelineTime));
						this.size(GuiPathing.this.timeline, this.x(GuiPathing.this.zoomButtonPanel) - 2, 20);
						this.pos(GuiPathing.this.scrollbar, 0,
								this.y(GuiPathing.this.timeline) + this.height(GuiPathing.this.timeline) + 1);
						this.size(GuiPathing.this.scrollbar, this.x(GuiPathing.this.zoomButtonPanel) - 2, 9);
					}
				})).addElements((LayoutData) null,
						new GuiElement[] { this.timeline, this.timelineTime, this.scrollbar, this.zoomButtonPanel });
		this.panel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout(HorizontalLayout.Alignment.CENTER)).setSpacing(5)))
				.addElements(new HorizontalLayout.Data(0.5D), new GuiElement[] { this.playPauseButton,
						this.renderButton, this.positionKeyframeButton, this.timeKeyframeButton, this.timelinePanel });
		this.prevSpeed = -1;
		this.prevTime = -1;
		this.core = core;
		this.mod = mod;
		this.replayHandler = replayHandler;
		this.overlay = replayHandler.getOverlay();
		this.player = new RealtimeTimelinePlayer(replayHandler);
		this.timeline.setLength((Integer) core.getSettingsRegistry().get(Setting.TIMELINE_LENGTH) * 1000);
		((GuiButton) this.playPauseButton.setSpriteUV(new ReadablePoint() {
			public int getX() {
				return 0;
			}

			public int getY() {
				return GuiPathing.this.player.isActive() ? 20 : 0;
			}

			public void getLocation(WritablePoint dest) {
				dest.setLocation(this.getX(), this.getY());
			}
		})).onClick(new Runnable() {
			public void run() {
				if (player.isActive()) {
					player.getFuture().cancel(false);
				} else {
					boolean ignoreTimeKeyframes = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

					Timeline timeline = preparePathsForPlayback(ignoreTimeKeyframes).okOrElse(err -> {
						GuiInfoPopup.open(overlay, err);
						return null;
					});
					if (timeline == null)
						return;

					Path timePath = new SPTimeline(timeline).getTimePath();
					timePath.setActive(!ignoreTimeKeyframes);

					// Start from cursor time unless the control key is pressed (then start from
					// beginning)
					int startTime = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 0
							: GuiPathing.this.timeline.getCursorPosition();
					ListenableFuture<Void> future = player.start(timeline, startTime);
					overlay.setCloseable(false);
					overlay.setMouseVisible(true);
					core.printInfoToChat("replaymod.chat.pathstarted");
					Futures.addCallback(future, new FutureCallback<Void>() {
						@Override
						public void onSuccess(@Nullable Void result) {
							if (future.isCancelled()) {
								core.printInfoToChat("replaymod.chat.pathinterrupted");
							} else {
								core.printInfoToChat("replaymod.chat.pathfinished");
							}
							overlay.setCloseable(true);
						}

						@Override
						public void onFailure(Throwable t) {
							if (!(t instanceof CancellationException)) {
								t.printStackTrace();
							}
							overlay.setCloseable(true);
						}
					}, Runnable::run);
				}
			}
		});
		((GuiButton) this.positionKeyframeButton.setSpriteUV(new ReadablePoint() {
			public int getX() {
				SPTimeline.SPPath keyframePath = mod.getSelectedPath();
				long keyframeTime = mod.getSelectedTime();
				if (keyframePath != SPTimeline.SPPath.POSITION) {
					keyframeTime = (long) GuiPathing.this.timeline.getCursorPosition();
					keyframePath = mod.getCurrentTimeline().isPositionKeyframe(keyframeTime)
							? SPTimeline.SPPath.POSITION
							: null;
				}

				if (keyframePath != SPTimeline.SPPath.POSITION) {
					return replayHandler.isCameraView() ? 0 : 40;
				} else {
					return mod.getCurrentTimeline().isSpectatorKeyframe(keyframeTime) ? 40 : 0;
				}
			}

			public int getY() {
				SPTimeline.SPPath keyframePath = mod.getSelectedPath();
				if (keyframePath != SPTimeline.SPPath.POSITION) {
					keyframePath = mod.getCurrentTimeline().isPositionKeyframe(
							(long) GuiPathing.this.timeline.getCursorPosition()) ? SPTimeline.SPPath.POSITION : null;
				}

				return keyframePath == SPTimeline.SPPath.POSITION ? 60 : 40;
			}

			public void getLocation(WritablePoint dest) {
				dest.setLocation(this.getX(), this.getY());
			}
		})).onClick(new Runnable() {
			public void run() {
				GuiPathing.this.toggleKeyframe(SPTimeline.SPPath.POSITION, false);
			}
		});
		((GuiButton) this.timeKeyframeButton.setSpriteUV(new ReadablePoint() {
			public int getX() {
				return 0;
			}

			public int getY() {
				SPTimeline.SPPath keyframePath = mod.getSelectedPath();
				if (keyframePath != SPTimeline.SPPath.TIME) {
					keyframePath = mod.getCurrentTimeline().isTimeKeyframe(
							(long) GuiPathing.this.timeline.getCursorPosition()) ? SPTimeline.SPPath.TIME : null;
				}

				return keyframePath == SPTimeline.SPPath.TIME ? 100 : 80;
			}

			public void getLocation(WritablePoint dest) {
				dest.setLocation(this.getX(), this.getY());
			}
		})).onClick(new Runnable() {
			public void run() {
				GuiPathing.this.toggleKeyframe(SPTimeline.SPPath.TIME, false);
			}
		});
		this.overlay.addElements((LayoutData) null, new GuiElement[] { this.panel });
		this.overlay.setLayout(new CustomLayout<GuiReplayOverlay>(this.overlay.getLayout()) {
			protected void layout(GuiReplayOverlay container, int width, int height) {
				GuiPathing.this.checkForAutoSync();
				this.pos(GuiPathing.this.panel, 10,
						this.y(GuiPathing.this.overlay.topPanel) + this.height(GuiPathing.this.overlay.topPanel) + 3);
				this.size(GuiPathing.this.panel, width - 20, 40);
			}
		});
		this.startLoadingEntityTracker();
	}

	private void abortPathPlayback() {
		if (this.player.isActive()) {
			ListenableFuture<Void> future = this.player.getFuture();
			if (!future.isDone() && !future.isCancelled()) {
				future.cancel(false);
			}

			this.player.onTick();
		}
	}

	public void keyframeRepoButtonPressed() {
		this.abortPathPlayback();

		try {
			TimelineSerialization serialization = new TimelineSerialization(this.mod.getCurrentTimeline(),
					(ReplayFile) null);
			String serialized = serialization
					.serialize(Collections.singletonMap("", this.mod.getCurrentTimeline().getTimeline()));
			Timeline currentTimeline = (Timeline) serialization.deserialize(serialized).get("");
			GuiKeyframeRepository gui = new GuiKeyframeRepository(this.mod.getCurrentTimeline(),
					this.replayHandler.getReplayFile(), currentTimeline);
			Futures.addCallback(gui.getFuture(), new FutureCallback<Timeline>() {
				public void onSuccess(Timeline result) {
					if (result != null) {
						GuiPathing.this.mod.setCurrentTimeline(new SPTimeline(result));
					}

				}

				public void onFailure(Throwable t) {
					t.printStackTrace();
					GuiPathing.this.core.printWarningToChat("Error loading timeline: " + t.getMessage());
				}
			}, Runnable::run);
			gui.display();
		} catch (IOException var5) {
			var5.printStackTrace();
			this.core.printWarningToChat("Error loading timeline: " + var5.getMessage());
		}

	}

	public void clearKeyframesButtonPressed() {
		GuiYesNoPopup
				.open(this.replayHandler.getOverlay(),
						((GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.clearcallback.title", new Object[0]))
								.setColor(Colors.BLACK))
				.setYesI18nLabel("gui.yes").setNoI18nLabel("gui.no").onAccept(() -> {
					this.mod.clearCurrentTimeline();
					if (this.entityTracker != null) {
						this.mod.getCurrentTimeline().setEntityTracker(this.entityTracker);
					}

				});
	}

	private void checkForAutoSync() {
		if (!this.mod.keySyncTime.isAutoActivating()) {
			this.prevSpeed = -1;
			this.prevTime = -1;
		} else {
			int speed = this.overlay.speedSlider.getValue();
			if (this.prevSpeed != speed && this.prevSpeed != -1) {
				this.syncTimeButtonPressed();
			}

			this.prevSpeed = speed;
			int time = this.replayHandler.getReplaySender().currentTimeStamp();
			if (this.prevTime != time && this.prevTime != -1 && !this.player.isActive()) {
				this.syncTimeButtonPressed();
			}

			this.prevTime = time;
		}
	}

	private Integer computeSyncTime(int cursor) {
		int time = this.replayHandler.getReplaySender().currentTimeStamp();
		Keyframe keyframe = (Keyframe) this.mod.getCurrentTimeline().getTimePath().getKeyframes().stream()
				.filter((it) -> {
					return it.getTime() <= (long) cursor;
				}).reduce((__, last) -> {
					return last;
				}).orElse(null);
		if (keyframe != null) {
			int keyframeCursor = (int) keyframe.getTime();
			int keyframeTime = (Integer) keyframe.getValue(TimestampProperty.PROPERTY).get();
			int timePassed = time - keyframeTime;
			double speed = MCVer.Keyboard.isKeyDown(340) ? 1.0D : this.replayHandler.getOverlay().getSpeedSliderValue();
			int cursorPassed = (int) ((double) timePassed / speed);
			return keyframeCursor + cursorPassed;
		} else {
			return null;
		}
	}

	public void syncTimeButtonPressed() {
		int cursor = this.timeline.getCursorPosition();
		Integer updatedCursor = this.computeSyncTime(cursor);
		if (updatedCursor != null) {
			cursor = updatedCursor;

			while (true) {
				updatedCursor = this.computeSyncTime(cursor);
				if (updatedCursor == null || updatedCursor == cursor) {
					((GuiKeyframeTimeline) this.timeline.setCursorPosition(cursor)).ensureCursorVisibleWithPadding();
					this.mod.setSelected((SPTimeline.SPPath) null, 0L);
					return;
				}

				if (updatedCursor < cursor) {
					return;
				}

				cursor = updatedCursor;
			}
		}
	}

	public boolean deleteButtonPressed() {
		if (this.mod.getSelectedPath() != null) {
			this.toggleKeyframe(this.mod.getSelectedPath(), false);
			return true;
		} else {
			return false;
		}
	}

	private void startLoadingEntityTracker() {
		Preconditions.checkState(this.entityTrackerFuture == null);
		this.entityTrackerFuture = SettableFuture.create();
		(new Thread(() -> {
			EntityPositionTracker tracker = new EntityPositionTracker(this.replayHandler.getReplayFile());

			try {
				long start = System.currentTimeMillis();
				tracker.load((p) -> {
					if (this.entityTrackerLoadingProgress != null) {
						this.entityTrackerLoadingProgress.accept(p);
					}

				});
				logger.info("Loaded entity tracker in " + (System.currentTimeMillis() - start) + "ms");
			} catch (Throwable var4) {
				logger.error("Loading entity tracker:", var4);
				this.mod.getCore().runLater(() -> {
					this.mod.getCore().printWarningToChat("Error loading entity tracker: %s",
							var4.getLocalizedMessage());
					this.entityTrackerFuture.setException(var4);
				});
				return;
			}

			this.entityTracker = tracker;
			this.mod.getCore().runLater(() -> {
				this.entityTrackerFuture.set(null);
			});
		})).start();
	}

	private Result<Timeline, String[]> preparePathsForPlayback(boolean ignoreTimeKeyframes) {
		SPTimeline spTimeline = this.mod.getCurrentTimeline();
		String[] errors = this.validatePathsForPlayback(spTimeline, ignoreTimeKeyframes);
		if (errors != null) {
			return Result.err(errors);
		} else {
			try {
				TimelineSerialization serialization = new TimelineSerialization(spTimeline, (ReplayFile) null);
				String serialized = serialization.serialize(Collections.singletonMap("", spTimeline.getTimeline()));
				Timeline timeline = (Timeline) serialization.deserialize(serialized).get("");
				timeline.getPaths().forEach(Path::updateAll);
				return Result.ok(timeline);
			} catch (Throwable var7) {
				Utils.error(ReplayModSimplePathing.LOGGER, this.replayHandler.getOverlay(),
						CrashReport.forThrowable(var7, "Cloning timeline"), () -> {
						});
				return Result.err(null);
			}
		}
	}

	private String[] validatePathsForPlayback(SPTimeline timeline, boolean ignoreTimeKeyframes) {
		timeline.getTimeline().getPaths().forEach(Path::updateAll);
		if (timeline.getPositionPath().getSegments().isEmpty()) {
			return new String[] { "replaymod.chat.morekeyframes" };
		} else if (ignoreTimeKeyframes) {
			return null;
		} else {
			int lastTime = 0;

			int time;
			for (Iterator var4 = timeline.getTimePath().getKeyframes().iterator(); var4.hasNext(); lastTime = time) {
				Keyframe keyframe = (Keyframe) var4.next();
				time = (Integer) keyframe.getValue(TimestampProperty.PROPERTY).orElseThrow(IllegalStateException::new);
				if (time < lastTime) {
					return new String[] { "replaymod.error.negativetime1", "replaymod.error.negativetime2",
							"replaymod.error.negativetime3" };
				}
			}

			if (timeline.getTimePath().getSegments().isEmpty()) {
				return new String[] { "replaymod.chat.morekeyframes" };
			} else {
				return null;
			}
		}
	}

	public void zoomTimeline(double factor) {
		this.scrollbar.setZoom(this.scrollbar.getZoom() * factor);
	}

	public boolean loadEntityTracker(Runnable thenRun) {
		if (this.entityTracker == null && !this.errorShown) {
			ReplayModSimplePathing.LOGGER.debug("Entity tracker not yet loaded, delaying...");
			final GuiPathing.LoadEntityTrackerPopup popup = new GuiPathing.LoadEntityTrackerPopup(
					this.replayHandler.getOverlay());
			this.entityTrackerLoadingProgress = (p) -> {
				popup.progressBar.setProgress(p.floatValue());
			};
			Futures.addCallback(this.entityTrackerFuture, new FutureCallback<Void>() {
				public void onSuccess(@Nullable Void result) {
					popup.close();
					if (GuiPathing.this.mod.getCurrentTimeline().getEntityTracker() == null) {
						GuiPathing.this.mod.getCurrentTimeline().setEntityTracker(GuiPathing.this.entityTracker);
					}

					thenRun.run();
				}

				public void onFailure(@Nonnull Throwable t) {
					if (!GuiPathing.this.errorShown) {
						String message = "Failed to load entity tracker, spectator keyframes will be broken.";
						GuiReplayOverlay overlay = GuiPathing.this.replayHandler.getOverlay();
						Utils.error(ReplayModSimplePathing.LOGGER, overlay, CrashReport.forThrowable(t, message),
								() -> {
									popup.close();
									thenRun.run();
								});
						GuiPathing.this.errorShown = true;
					} else {
						thenRun.run();
					}

				}
			}, Runnable::run);
			return false;
		} else {
			if (this.mod.getCurrentTimeline().getEntityTracker() == null) {
				this.mod.getCurrentTimeline().setEntityTracker(this.entityTracker);
			}

			return true;
		}
	}

	public void toggleKeyframe(SPTimeline.SPPath path, boolean neverSpectator) {
		ReplayModSimplePathing.LOGGER.debug("Updating keyframe on path {}" + path);
		if (this.loadEntityTracker(() -> {
			this.toggleKeyframe(path, neverSpectator);
		})) {
			int time = this.timeline.getCursorPosition();
			SPTimeline timeline = this.mod.getCurrentTimeline();
			if (timeline.getPositionPath().getKeyframes().isEmpty() && timeline.getTimePath().getKeyframes().isEmpty()
					&& time > 1000) {
				String text = I18n.get("replaymod.gui.ingame.first_keyframe_not_at_start_warning", new Object[0]);
				GuiInfoPopup.open(this.overlay, (String[]) text.split("\\\\n"));
			}

			switch (path) {
			case TIME:
				if (this.mod.getSelectedPath() == path) {
					ReplayModSimplePathing.LOGGER.debug("Selected keyframe is time keyframe -> removing keyframe");
					timeline.removeTimeKeyframe(this.mod.getSelectedTime());
					this.mod.setSelected((SPTimeline.SPPath) null, 0L);
				} else if (timeline.isTimeKeyframe((long) time)) {
					ReplayModSimplePathing.LOGGER
							.debug("Keyframe at cursor position is time keyframe -> removing keyframe");
					timeline.removeTimeKeyframe((long) time);
					this.mod.setSelected((SPTimeline.SPPath) null, 0L);
				} else {
					ReplayModSimplePathing.LOGGER.debug("No time keyframe found -> adding new keyframe");
					timeline.addTimeKeyframe((long) time, this.replayHandler.getReplaySender().currentTimeStamp());
					this.mod.setSelected(path, (long) time);
				}
				break;
			case POSITION:
				if (this.mod.getSelectedPath() == path) {
					ReplayModSimplePathing.LOGGER.debug("Selected keyframe is position keyframe -> removing keyframe");
					timeline.removePositionKeyframe(this.mod.getSelectedTime());
					this.mod.setSelected((SPTimeline.SPPath) null, 0L);
				} else if (timeline.isPositionKeyframe((long) time)) {
					ReplayModSimplePathing.LOGGER
							.debug("Keyframe at cursor position is position keyframe -> removing keyframe");
					timeline.removePositionKeyframe((long) time);
					this.mod.setSelected((SPTimeline.SPPath) null, 0L);
				} else {
					ReplayModSimplePathing.LOGGER.debug("No position keyframe found -> adding new keyframe");
					CameraEntity camera = this.replayHandler.getCameraEntity();
					int spectatedId = -1;
					if (!this.replayHandler.isCameraView() && !neverSpectator) {
						spectatedId = this.replayHandler.getOverlay().getMinecraft().getCameraEntity().getId();
					}

					timeline.addPositionKeyframe((long) time, camera.getX(), camera.getY(), camera.getZ(),
							camera.getYRot(), camera.getXRot(), camera.roll, spectatedId);
					this.mod.setSelected(path, (long) time);
				}
			}

		}
	}

	public ReplayModSimplePathing getMod() {
		return this.mod;
	}

	public EntityPositionTracker getEntityTracker() {
		return this.entityTracker;
	}

	public void openEditKeyframePopup(SPTimeline.SPPath path, long time) {
		if (this.loadEntityTracker(() -> {
			this.openEditKeyframePopup(path, time);
		})) {
			Keyframe keyframe = this.mod.getCurrentTimeline().getKeyframe(path, time);
			if (keyframe.getProperties().contains(SpectatorProperty.PROPERTY)) {
				(new GuiEditKeyframe.Spectator(this, path, keyframe.getTime())).open();
			} else if (keyframe.getProperties().contains(CameraProperties.POSITION)) {
				(new GuiEditKeyframe.Position(this, path, keyframe.getTime())).open();
			} else {
				(new GuiEditKeyframe.Time(this, path, keyframe.getTime())).open();
			}

		}
	}

	private class LoadEntityTrackerPopup extends AbstractGuiPopup<GuiPathing.LoadEntityTrackerPopup> {
		private final GuiProgressBar progressBar;

		public LoadEntityTrackerPopup(GuiContainer container) {
			super(container);
			this.progressBar = (GuiProgressBar) ((GuiProgressBar) (new GuiProgressBar(this.popup)).setSize(300, 20))
					.setI18nLabel("replaymod.gui.loadentitytracker", new Object[0]);
			this.open();
		}

		public void close() {
			super.close();
		}

		protected GuiPathing.LoadEntityTrackerPopup getThis() {
			return this;
		}
	}
}

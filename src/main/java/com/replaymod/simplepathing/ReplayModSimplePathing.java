package com.replaymod.simplepathing;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.Module;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.events.SettingsChangedCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.events.ReplayClosedCallback;
import com.replaymod.replay.events.ReplayClosingCallback;
import com.replaymod.replay.events.ReplayOpenedCallback;
import com.replaymod.replay.gui.overlay.GuiReplayOverlay;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.simplepathing.gui.GuiPathing;
import com.replaymod.simplepathing.preview.PathPreview;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.screens.Screen;

public class ReplayModSimplePathing extends EventRegistrations implements Module {
	public static ReplayModSimplePathing instance;
	private ReplayMod core;
	public KeyBindingRegistry.Binding keyPositionKeyframe;
	public KeyBindingRegistry.Binding keyTimeKeyframe;
	public KeyBindingRegistry.Binding keySyncTime;
	public static Logger LOGGER = LogManager.getLogger();
	private GuiPathing guiPathing;
	private PathPreview pathPreview;
	private SPTimeline currentTimeline;
	private SPTimeline.SPPath selectedPath;
	private long selectedTime;
	private final AtomicInteger lastSaveId;
	private ExecutorService saveService;
	private SPTimeline lastTimeline;
	private Change lastChange;

	public ReplayModSimplePathing(ReplayMod core) {
		instance = this;
		this.pathPreview = new PathPreview(this);
		this.on(ReplayOpenedCallback.EVENT, this::onReplayOpened);
		this.on(ReplayClosingCallback.EVENT, (replayHandler) -> {
			this.onReplayClosing();
		});
		this.on(ReplayClosedCallback.EVENT, (replayHandler) -> {
			this.onReplayClosed();
		});
		this.lastSaveId = new AtomicInteger();
		this.core = core;
		core.getSettingsRegistry().register(Setting.class);
		this.on(SettingsChangedCallback.EVENT, (registry, key) -> {
			if (key == Setting.DEFAULT_INTERPOLATION && this.currentTimeline != null && this.guiPathing != null) {
				this.updateDefaultInterpolatorType();
			}

		});
	}

	public void register() {
		super.register();
		this.pathPreview.register();
	}

	public void unregister() {
		super.unregister();
		this.pathPreview.unregister();
	}

	public void initClient() {
		this.register();
	}

	public void registerKeyBindings(KeyBindingRegistry registry) {
		this.pathPreview.registerKeyBindings(registry);
		this.core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.keyframerepository", 88, () -> {
			if (this.guiPathing != null) {
				this.guiPathing.keyframeRepoButtonPressed();
			}

		}, true);
		this.core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.clearkeyframes", 67, () -> {
			if (this.guiPathing != null) {
				this.guiPathing.clearKeyframesButtonPressed();
			}

		}, true);
		this.keySyncTime = this.core.getKeyBindingRegistry().registerRepeatedKeyBinding("replaymod.input.synctimeline",
				86, () -> {
					if (this.guiPathing != null) {
						this.guiPathing.syncTimeButtonPressed();
					}

				}, true);
		SettingsRegistry settingsRegistry = this.core.getSettingsRegistry();
		this.keySyncTime.registerAutoActivationSupport((Boolean) settingsRegistry.get(Setting.AUTO_SYNC), (active) -> {
			settingsRegistry.set(Setting.AUTO_SYNC, active);
			settingsRegistry.save();
		});
		this.core.getKeyBindingRegistry().registerRaw(261, () -> {
			return this.guiPathing != null && this.guiPathing.deleteButtonPressed();
		});
		this.keyPositionKeyframe = this.core.getKeyBindingRegistry()
				.registerKeyBinding("replaymod.input.positionkeyframe", 73, () -> {
					if (this.guiPathing != null) {
						this.guiPathing.toggleKeyframe(SPTimeline.SPPath.POSITION, false);
					}

				}, true);
		this.core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.positiononlykeyframe", 0, () -> {
			if (this.guiPathing != null) {
				this.guiPathing.toggleKeyframe(SPTimeline.SPPath.POSITION, true);
			}

		}, true);
		this.keyTimeKeyframe = this.core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.timekeyframe", 79,
				() -> {
					if (this.guiPathing != null) {
						this.guiPathing.toggleKeyframe(SPTimeline.SPPath.TIME, false);
					}

				}, true);
		this.core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.bothkeyframes", 0, () -> {
			if (this.guiPathing != null) {
				this.guiPathing.toggleKeyframe(SPTimeline.SPPath.TIME, false);
				this.guiPathing.toggleKeyframe(SPTimeline.SPPath.POSITION, false);
			}

		}, true);
		this.core.getKeyBindingRegistry().registerRaw(90, () -> {
			if (Screen.hasControlDown() && this.currentTimeline != null) {
				Timeline timeline = this.currentTimeline.getTimeline();
				if (Screen.hasShiftDown()) {
					if (timeline.peekRedoStack() != null) {
						timeline.redoLastChange();
					}
				} else if (timeline.peekUndoStack() != null) {
					timeline.undoLastChange();
				}

				return true;
			} else {
				return false;
			}
		});
		this.core.getKeyBindingRegistry().registerRaw(89, () -> {
			if (Screen.hasControlDown() && this.currentTimeline != null) {
				Timeline timeline = this.currentTimeline.getTimeline();
				if (timeline.peekRedoStack() != null) {
					timeline.redoLastChange();
				}

				return true;
			} else {
				return false;
			}
		});
	}

	private void onReplayOpened(ReplayHandler replayHandler) {
		final ReplayFile replayFile = replayHandler.getReplayFile();

		try {
			synchronized (replayFile) {
				Timeline timeline = (Timeline) replayFile.getTimelines(new SPTimeline()).get("");
				if (timeline != null) {
					this.setCurrentTimeline(new SPTimeline(timeline), false);
				} else {
					this.setCurrentTimeline(new SPTimeline(), false);
				}
			}
		} catch (IOException var7) {
			throw new ReportedException(CrashReport.forThrowable(var7, "Reading timeline"));
		}

		this.guiPathing = new GuiPathing(this.core, this, replayHandler);
		this.saveService = Executors.newSingleThreadExecutor();
		(new Runnable() {
			public void run() {
				ReplayModSimplePathing.this.maybeSaveTimeline(replayFile);
				if (ReplayModSimplePathing.this.guiPathing != null) {
					ReplayModSimplePathing.this.core.runLater(this);
				}

			}
		}).run();
	}

	private void onReplayClosing() {
		this.saveService.shutdown();

		try {
			this.saveService.awaitTermination(1L, TimeUnit.MINUTES);
		} catch (InterruptedException var2) {
			Thread.currentThread().interrupt();
		}

		this.saveService = null;
	}

	private void onReplayClosed() {
		this.currentTimeline = null;
		this.guiPathing = null;
		this.selectedPath = null;
	}

	private GuiReplayOverlay getReplayOverlay() {
		return ReplayModReplay.instance.getReplayHandler().getOverlay();
	}

	public SPTimeline.SPPath getSelectedPath() {
		if (this.getReplayOverlay().timeline.getSelectedMarker() != null) {
			this.selectedPath = null;
			this.selectedTime = 0L;
		}

		return this.selectedPath;
	}

	public long getSelectedTime() {
		return this.selectedTime;
	}

	public boolean isSelected(Keyframe keyframe) {
		return this.getSelectedPath() != null
				&& this.currentTimeline.getKeyframe(this.selectedPath, this.selectedTime) == keyframe;
	}

	public void setSelected(SPTimeline.SPPath path, long time) {
		this.selectedPath = path;
		this.selectedTime = time;
		if (this.selectedPath != null) {
			this.getReplayOverlay().timeline.setSelectedMarker((Marker) null);
		}

	}

	public void setCurrentTimeline(SPTimeline newTimeline) {
		this.setCurrentTimeline(newTimeline, true);
	}

	private void setCurrentTimeline(SPTimeline newTimeline, boolean save) {
		this.selectedPath = null;
		this.currentTimeline = newTimeline;
		if (!save) {
			this.lastTimeline = newTimeline;
			this.lastChange = newTimeline.getTimeline().peekUndoStack();
		}

		this.updateDefaultInterpolatorType();
	}

	public void clearCurrentTimeline() {
		this.setCurrentTimeline(new SPTimeline());
	}

	public SPTimeline getCurrentTimeline() {
		return this.currentTimeline;
	}

	private void updateDefaultInterpolatorType() {
		InterpolatorType newDefaultType = InterpolatorType
				.fromString((String) this.core.getSettingsRegistry().get(Setting.DEFAULT_INTERPOLATION));
		this.currentTimeline.setDefaultInterpolatorType(newDefaultType);
	}

	public ReplayMod getCore() {
		return this.core;
	}

	public GuiPathing getGuiPathing() {
		return this.guiPathing;
	}

	private void maybeSaveTimeline(ReplayFile replayFile) {
		SPTimeline spTimeline = this.currentTimeline;
		if (spTimeline != null && this.saveService != null) {
			Change latestChange = spTimeline.getTimeline().peekUndoStack();
			if (spTimeline != this.lastTimeline || latestChange != this.lastChange) {
				this.lastTimeline = spTimeline;
				this.lastChange = latestChange;

				Timeline timeline;
				try {
					TimelineSerialization serialization = new TimelineSerialization(spTimeline, (ReplayFile) null);
					String serialized = serialization.serialize(Collections.singletonMap("", spTimeline.getTimeline()));
					timeline = (Timeline) serialization.deserialize(serialized).get("");
				} catch (Throwable var7) {
					CrashReport report = CrashReport.forThrowable(var7, "Cloning timeline");
					throw new ReportedException(report);
				}

				int id = this.lastSaveId.incrementAndGet();
				this.saveService.submit(() -> {
					if (this.lastSaveId.get() == id) {
						try {
							this.saveTimeline(replayFile, spTimeline, timeline);
						} catch (IOException var6) {
							LOGGER.error("Auto-saving timeline:", var6);
						}

					}
				});
			}
		} else {
			this.lastTimeline = null;
			this.lastChange = null;
		}
	}

	private void saveTimeline(ReplayFile replayFile, PathingRegistry pathingRegistry, Timeline timeline)
			throws IOException {
		synchronized (replayFile) {
			Map<String, Timeline> timelineMap = replayFile.getTimelines(pathingRegistry);
			timelineMap.put("", timeline);
			replayFile.writeTimelines(pathingRegistry, timelineMap);
		}
	}
}

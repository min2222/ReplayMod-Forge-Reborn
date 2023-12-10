package com.replaymod.pathing.player;

import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.mixin.MinecraftAccessor;
import com.replaymod.replay.mixin.TimerAccessor;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;

public abstract class AbstractTimelinePlayer extends EventRegistrations {
	private final Minecraft mc = MCVer.getMinecraft();
	private final ReplayHandler replayHandler;
	private Timeline timeline;
	protected long startOffset;
	private boolean wasAsyncMode;
	private long lastTime;
	private long lastTimestamp;
	private ListenableFuture<Void> future;
	private SettableFuture<Void> settableFuture;

	public AbstractTimelinePlayer(ReplayHandler replayHandler) {
		this.on(ReplayTimer.UpdatedCallback.EVENT, this::onTick);
		this.replayHandler = replayHandler;
	}

	public ListenableFuture<Void> start(Timeline timeline, long from) {
		this.startOffset = from;
		return this.start(timeline);
	}

	public ListenableFuture<Void> start(Timeline timeline) {
		this.timeline = timeline;
		Iterator<Keyframe> iter = Iterables
				.concat(Iterables.transform(timeline.getPaths(), new Function<Path, Iterable<Keyframe>>() {
					@Nullable
					public Iterable<Keyframe> apply(@Nullable Path input) {
						assert input != null;

						return input.getKeyframes();
					}
				})).iterator();
		if (!iter.hasNext()) {
			this.lastTimestamp = 0L;
		} else {
			this.lastTimestamp = ((Keyframe) (new Ordering<Keyframe>() {
				public int compare(@Nullable Keyframe left, @Nullable Keyframe right) {
					assert left != null;

					assert right != null;

					return Longs.compare(left.getTime(), right.getTime());
				}
			}).max(iter)).getTime();
		}

		this.wasAsyncMode = this.replayHandler.getReplaySender().isAsyncMode();
		this.replayHandler.getReplaySender().setSyncModeAndWait();
		this.register();
		this.lastTime = 0L;
		MinecraftAccessor mcA = (MinecraftAccessor) this.mc;
		ReplayTimer timer = new ReplayTimer(mcA.getTimer());
		mcA.setTimer(timer);
		TimerAccessor timerA = (TimerAccessor) timer;
		timerA.setTickLength(WrappedTimer.DEFAULT_MS_PER_TICK);
		timer.partialTick = (float) (timer.ticksThisFrame = 0);
		return this.future = this.settableFuture = SettableFuture.create();
	}

	public ListenableFuture<Void> getFuture() {
		return this.future;
	}

	public boolean isActive() {
		return this.future != null && !this.future.isDone();
	}

	public void onTick() {
		if (this.future.isDone()) {
			MinecraftAccessor mcA = (MinecraftAccessor) this.mc;
			mcA.setTimer(((ReplayTimer) mcA.getTimer()).getWrapped());
			this.replayHandler.getReplaySender().setReplaySpeed(0.0D);
			if (this.wasAsyncMode) {
				this.replayHandler.getReplaySender().setAsyncMode(true);
			}

			this.unregister();
		} else {
			long time = this.getTimePassed();
			if (time > this.lastTimestamp) {
				time = this.lastTimestamp;
			}

			this.timeline.applyToGame(time, this.replayHandler);
			this.timeline.applyToGame(time, this.replayHandler);
			long replayTime = (long) this.replayHandler.getReplaySender().currentTimeStamp();
			if (this.lastTime == 0L) {
				this.lastTime = replayTime;
			}

			float timeInTicks = (float) replayTime / 50.0F;
			float previousTimeInTicks = (float) this.lastTime / 50.0F;
			float passedTicks = timeInTicks - previousTimeInTicks;
			Timer renderTickCounter = ((MinecraftAccessor) this.mc).getTimer();
			if (renderTickCounter instanceof ReplayTimer) {
				ReplayTimer timer = (ReplayTimer) renderTickCounter;
				timer.partialTick += passedTicks;
				timer.ticksThisFrame = (int) timer.partialTick;
				timer.partialTick -= (float) timer.ticksThisFrame;
			}

			this.lastTime = replayTime;
			if (time >= this.lastTimestamp) {
				this.settableFuture.set(null);
			}

		}
	}

	public abstract long getTimePassed();
}

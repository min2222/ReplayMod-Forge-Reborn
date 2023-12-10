package com.replaymod.core.versions.scheduler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.replaymod.replay.mixin.MinecraftAccessor;

import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

public class SchedulerImpl implements Scheduler {
	private static final Minecraft mc = Minecraft.getInstance();
	private boolean inRunLater = false;
	private boolean inRenderTaskQueue = false;
	public final SchedulerImpl.ReplayModExecutor executor = new SchedulerImpl.ReplayModExecutor("Client/ReplayMod");

	public void runSync(Runnable runnable) throws InterruptedException, ExecutionException, TimeoutException {
		if (mc.isSameThread()) {
			runnable.run();
		} else {
			this.executor.submit(() -> {
				runnable.run();
				return null;
			}).get(30L, TimeUnit.SECONDS);
		}

	}

	public void runPostStartup(Runnable runnable) {
		this.runLater(new Runnable() {
			public void run() {
				if (SchedulerImpl.mc.getOverlay() != null) {
					SchedulerImpl.this.runLater(this);
				} else {
					runnable.run();
				}
			}
		});
	}

	public void runTasks() {
		this.executor.runAllTasks();
	}

	public void runLaterWithoutLock(Runnable runnable) {
		this.runLater(runnable);
	}

	public void runLater(Runnable runnable) {
		this.runLater(runnable, () -> {
			this.runLater(runnable);
		});
	}

	private void runLater(Runnable runnable, Runnable defer) {
		if (mc.isSameThread() && this.inRunLater && !this.inRenderTaskQueue) {
			((MinecraftAccessor) mc).getRenderTaskQueue().offer(() -> {
				this.inRenderTaskQueue = true;

				try {
					defer.run();
				} finally {
					this.inRenderTaskQueue = false;
				}

			});
		} else {
			this.executor.tell(() -> {
				this.inRunLater = true;

				try {
					runnable.run();
				} catch (ReportedException var6) {
					var6.printStackTrace();
					System.err.println(var6.getReport().getFriendlyReport());
					mc.delayCrashRaw(var6.getReport());
				} finally {
					this.inRunLater = false;
				}

			});
		}

	}

	public static class ReplayModExecutor extends ReentrantBlockableEventLoop<Runnable> {
		private final Thread mcThread = Thread.currentThread();

		private ReplayModExecutor(String string_1) {
			super(string_1);
		}

		protected Runnable wrapRunnable(Runnable runnable) {
			return runnable;
		}

		protected boolean shouldRun(Runnable runnable) {
			return true;
		}

		protected Thread getRunningThread() {
			return this.mcThread;
		}

		public void runAllTasks() {
			super.runAllTasks();
		}
	}
}

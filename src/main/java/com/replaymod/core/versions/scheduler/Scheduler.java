package com.replaymod.core.versions.scheduler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface Scheduler {
	void runSync(Runnable runnable) throws InterruptedException, ExecutionException, TimeoutException;

	void runPostStartup(Runnable runnable);

	void runLaterWithoutLock(Runnable runnable);

	void runLater(Runnable runnable);

	void runTasks();
}

package com.replaymod.core.files;

import java.io.IOException;

import com.replaymod.replaystudio.replay.ReplayFile;

public class ManagedReplayFile extends DelegatingReplayFile {
	private Runnable onClose;

	public ManagedReplayFile(ReplayFile delegate, Runnable onClose) {
		super(delegate);
		this.onClose = onClose;
	}

	public void close() throws IOException {
		super.close();
		this.onClose.run();
		this.onClose = () -> {
		};
	}
}

package com.replaymod.render.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class StreamPipe extends Thread {
	private final InputStream in;
	private final OutputStream out;

	public StreamPipe(InputStream in, OutputStream out) {
		super("StreamPipe from " + in + " to " + out);
		this.in = in;
		this.out = out;
	}

	public void run() {
		try {
			IOUtils.copy(this.in, this.out);
		} catch (IOException var2) {
		}

	}
}

package com.replaymod.render;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.Validate;

import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.rendering.Channel;
import com.replaymod.render.rendering.FrameConsumer;
import com.replaymod.render.rendering.VideoRenderer;
import com.replaymod.render.utils.ByteBufferPool;
import com.replaymod.render.utils.StreamPipe;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;

public class FFmpegWriter implements FrameConsumer<BitmapFrame> {
	private final VideoRenderer renderer;
	private final RenderSettings settings;
	private final Process process;
	private final OutputStream outputStream;
	private final WritableByteChannel channel;
	private final String commandArgs;
	private volatile boolean aborted;
	private ByteArrayOutputStream ffmpegLog = new ByteArrayOutputStream(4096);

	public FFmpegWriter(VideoRenderer renderer) throws IOException {
		this.renderer = renderer;
		this.settings = renderer.getRenderSettings();
		File outputFolder = this.settings.getOutputFile().getParentFile();
		FileUtils.forceMkdir(outputFolder);
		String fileName = this.settings.getOutputFile().getName();
		this.commandArgs = this.settings.getExportArguments()
				.replace("%WIDTH%", String.valueOf(this.settings.getVideoWidth()))
				.replace("%HEIGHT%", String.valueOf(this.settings.getVideoHeight()))
				.replace("%FPS%", String.valueOf(this.settings.getFramesPerSecond())).replace("%FILENAME%", fileName)
				.replace("%BITRATE%", String.valueOf(this.settings.getBitRate()))
				.replace("%FILTERS%", this.settings.getVideoFilters());
		String executable = this.settings.getExportCommandOrDefault();
		ReplayModRender.LOGGER.info("Starting {} with args: {}", executable, this.commandArgs);

		String[] cmdline;
		try {
			cmdline = (new CommandLine(executable)).addArguments(this.commandArgs, false).toStrings();
		} catch (IllegalArgumentException var9) {
			ReplayModRender.LOGGER.error("Failed to parse ffmpeg command line:", var9);
			throw new FFmpegWriter.FFmpegStartupException(this.settings, var9.getLocalizedMessage());
		}

		try {
			this.process = (new ProcessBuilder(cmdline)).directory(outputFolder).start();
		} catch (IOException var8) {
			throw new FFmpegWriter.NoFFmpegException(var8);
		}

		File exportLogFile = new File(MCVer.getMinecraft().gameDirectory, "export.log");
		OutputStream exportLogOut = new TeeOutputStream(new FileOutputStream(exportLogFile), this.ffmpegLog);
		(new StreamPipe(this.process.getInputStream(), exportLogOut)).start();
		(new StreamPipe(this.process.getErrorStream(), exportLogOut)).start();
		this.outputStream = this.process.getOutputStream();
		this.channel = Channels.newChannel(this.outputStream);
	}

	public void close() throws IOException {
		IOUtils.closeQuietly(this.outputStream);
		long startTime = System.nanoTime();
		long rem = TimeUnit.SECONDS.toNanos(30L);

		while (true) {
			try {
				this.process.exitValue();
				break;
			} catch (IllegalThreadStateException var8) {
				if (rem > 0L) {
					try {
						Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1L, 100L));
					} catch (InterruptedException var7) {
						Thread.currentThread().interrupt();
						break;
					}
				}

				rem = TimeUnit.SECONDS.toNanos(30L) - (System.nanoTime() - startTime);
				if (rem <= 0L) {
					break;
				}
			}
		}

		this.process.destroy();
	}

	public void consume(Map<Channel, BitmapFrame> channels) {
		BitmapFrame frame = (BitmapFrame) channels.get(Channel.BRGA);

		try {
			this.checkSize(frame.getSize());
			this.channel.write(frame.getByteBuffer());
			return;
		} catch (Throwable var11) {
			if (!this.aborted) {
				try {
					this.getVideoFile();
				} catch (FFmpegWriter.FFmpegStartupException var10) {
					this.renderer.setFailure(var10);
					return;
				}

				CrashReport report = CrashReport.forThrowable(var11, "Exporting frame");
				CrashReportCategory exportDetails = report.addCategory("Export details");
				RenderSettings var10002 = this.settings;
				Objects.requireNonNull(var10002);
				exportDetails.setDetail("Export command", var10002::getExportCommand);
				String var13 = this.commandArgs;
				Objects.requireNonNull(var13);
				exportDetails.setDetail("Export args", var13::toString);
				MCVer.getMinecraft().delayCrashRaw(report);
				return;
			}
		} finally {
			channels.values().forEach((it) -> {
				ByteBufferPool.release(it.getByteBuffer());
			});
		}

	}

	public boolean isParallelCapable() {
		return false;
	}

	private void checkSize(ReadableDimension size) {
		this.checkSize(size.getWidth(), size.getHeight());
	}

	private void checkSize(int width, int height) {
		Validate.isTrue(width == this.settings.getVideoWidth(), "Width has to be %d but was %d",
				this.settings.getVideoWidth(), width);
		Validate.isTrue(height == this.settings.getVideoHeight(), "Height has to be %d but was %d",
				this.settings.getVideoHeight(), height);
	}

	public void abort() {
		this.aborted = true;
	}

	public File getVideoFile() throws FFmpegWriter.FFmpegStartupException {
		String log = this.ffmpegLog.toString();
		String[] var2 = log.split("\n");
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			String line = var2[var4];
			if (line.startsWith("Output #0")) {
				String fileName = line.substring(line.indexOf(", to '") + 6, line.lastIndexOf(39));
				return new File(this.settings.getOutputFile().getParentFile(), fileName);
			}
		}

		throw new FFmpegWriter.FFmpegStartupException(this.settings, log);
	}

	public static class FFmpegStartupException extends IOException {
		private final RenderSettings settings;
		private final String log;

		public FFmpegStartupException(RenderSettings settings, String log) {
			this.settings = settings;
			this.log = log;
		}

		public RenderSettings getSettings() {
			return this.settings;
		}

		public String getLog() {
			return this.log;
		}
	}

	public static class NoFFmpegException extends IOException {
		public NoFFmpegException(Throwable cause) {
			super(cause);
		}
	}
}

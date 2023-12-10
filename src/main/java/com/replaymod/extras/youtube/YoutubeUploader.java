package com.replaymod.extras.youtube;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.extras.ReplayModExtras;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.metadata.MetadataInjector;

import net.minecraft.client.Minecraft;

public class YoutubeUploader {
	private static final String CLIENT_ID = "743126594724-mfe7pj1k7e47uu5pk4503c8st9vj9ibu.apps.googleusercontent.com";
	private static final String CLIENT_SECRET = "gMwcy3mRYCRamCIjJIYP7rqc";
	private static final String FFMPEG_MP4 = "-i %s -c:v libx264 -preset slow -crf 16 %s";
	private static final JsonFactory JSON_FACTORY = new GsonFactory();
	private final NetHttpTransport httpTransport;
	private final DataStoreFactory dataStoreFactory;
	private final File videoFile;
	private final int videoFrames;
	private final String thumbnailFormat;
	private final byte[] thumbnailImage;
	private final RenderSettings settings;
	private final VideoSnippet videoSnippet;
	private final VideoVisibility videoVisibility;
	private Thread thread;
	@NonNull
	private Supplier<Double> progress = Suppliers.ofInstance(0.0D);
	private YoutubeUploader.State state;
	private volatile boolean cancelled;

	public YoutubeUploader(Minecraft minecraft, File videoFile, int videoFrames, String thumbnailFormat,
			byte[] thumbnailImage, RenderSettings settings, VideoVisibility videoVisibility, VideoSnippet videoSnippet)
			throws GeneralSecurityException, IOException {
		this.videoFile = videoFile;
		this.videoFrames = videoFrames;
		this.thumbnailImage = thumbnailImage;
		this.thumbnailFormat = thumbnailFormat;
		this.settings = settings;
		this.videoVisibility = videoVisibility;
		this.videoSnippet = videoSnippet;
		this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		this.dataStoreFactory = new FileDataStoreFactory(minecraft.gameDirectory);
	}

	public ListenableFuture<Video> upload() throws IOException {
		this.cancelled = false;
		SettableFuture<Video> future = SettableFuture.create();
		this.thread = new Thread(() -> {
			try {
				this.state = YoutubeUploader.State.AUTH;
				Credential credential = this.auth();
				this.state = YoutubeUploader.State.PREPARE_VIDEO;
				File processedFile = this.preUpload();
				this.progress = Suppliers.ofInstance(0.0D);
				YouTube youTube = (new YouTube.Builder(this.httpTransport, JSON_FACTORY, credential))
						.setApplicationName("ReplayMod").build();
				this.state = YoutubeUploader.State.UPLOAD;
				Video video = this.doUpload(youTube, processedFile);
				if (this.thumbnailImage != null) {
					this.doThumbUpload(youTube, video);
				}

				this.state = YoutubeUploader.State.CLEANUP;
				this.postUpload(processedFile);
				future.set(video);
			} catch (Throwable var6) {
				future.setException(var6);
			}

		});
		this.thread.start();
		return future;
	}

	public void cancel() throws InterruptedException {
		this.thread.stop();
		this.cancelled = true;
	}

	private Credential auth() throws IOException {
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
				CLIENT_ID, CLIENT_SECRET, Collections.singleton(YouTubeScopes.YOUTUBE_UPLOAD))
				.setDataStoreFactory(dataStoreFactory).build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	private File preUpload() throws InterruptedException, IOException {
		File outputFile = this.videoFile;
		if (this.settings.getRenderMethod().isSpherical()) {
			boolean isMp4 = Files.getFileExtension(outputFile.getName()).equalsIgnoreCase("mp4");
			if (!isMp4) {
				outputFile = new File(outputFile.getParentFile(), System.currentTimeMillis() + ".mp4");
				outputFile.deleteOnExit();
				String args = String.format("-i %s -c:v libx264 -preset slow -crf 16 %s", this.videoFile.getName(),
						outputFile.getName());
				CommandLine commandLine = new CommandLine(this.settings.getExportCommandOrDefault());
				commandLine.addArguments(args);
				ReplayModExtras.LOGGER.info("Re-encoding for metadata injection with {} {}",
						commandLine.getExecutable(), args);
				Process process = (new ProcessBuilder(commandLine.toStrings())).directory(outputFile.getParentFile())
						.start();
				AtomicBoolean active = new AtomicBoolean(true);
				InputStream in = process.getErrorStream();
				(new Thread(() -> {
					try {
						StringBuilder sb = new StringBuilder();

						while (active.get()) {
							char c = (char) in.read();
							if (c == '\r') {
								String str = sb.toString();
								ReplayModExtras.LOGGER.debug("[FFmpeg] {}", str);
								if (str.startsWith("frame=")) {
									str = str.substring(6).trim();
									str = str.substring(0, str.indexOf(32));
									double frame = (double) Integer.parseInt(str);
									this.progress = Suppliers.ofInstance(frame / (double) this.videoFrames);
								}

								sb = new StringBuilder();
							} else {
								sb.append(c);
							}
						}
					} catch (IOException var8) {
						var8.printStackTrace();
					}

				})).start();

				int result;
				try {
					result = process.waitFor();
				} catch (InterruptedException var13) {
					process.destroy();
					throw var13;
				} finally {
					active.set(false);
				}

				if (result != 0) {
					throw new IOException("FFmpeg returned: " + result);
				}
			}

			if (!this.settings.isInjectSphericalMetadata() || !isMp4) {
				MetadataInjector.injectMetadata(this.settings.getRenderMethod(), outputFile,
						this.settings.getTargetVideoWidth(), this.settings.getTargetVideoHeight(),
						this.settings.getSphericalFovX(), this.settings.getSphericalFovY());
			}
		}

		return outputFile;
	}

	private Video doUpload(YouTube youTube, File processedFile) throws IOException {
		Video video = new Video();
		VideoStatus videoStatus = new VideoStatus();
		videoStatus.setPrivacyStatus(this.videoVisibility.name().toLowerCase());
		video.setStatus(videoStatus);
		video.setSnippet(this.videoSnippet);
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(processedFile));

		Video result;
		try {
			InputStreamContent content = new InputStreamContent("video/*", inputStream);
			content.setLength(processedFile.length());
			YouTube.Videos.Insert videoInsert = youTube.videos().insert("snippet,statistics,status", video, content);
			MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
			this.progress = () -> {
				try {
					return uploader.getProgress();
				} catch (IOException var2) {
					throw new RuntimeException(var2);
				}
			};
			result = (Video) videoInsert.execute();
		} catch (Throwable var11) {
			try {
				inputStream.close();
			} catch (Throwable var10) {
				var11.addSuppressed(var10);
			}

			throw var11;
		}

		inputStream.close();
		return result;
	}

	private void doThumbUpload(YouTube youTube, Video video) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(this.thumbnailImage);

			try {
				InputStreamContent content = new InputStreamContent("image/" + this.thumbnailFormat, inputStream);
				content.setLength((long) inputStream.available());
				youTube.thumbnails().set(video.getId(), content).execute();
			} catch (Throwable var7) {
				try {
					inputStream.close();
				} catch (Throwable var6) {
					var7.addSuppressed(var6);
				}

				throw var7;
			}

			inputStream.close();
		} catch (GoogleJsonResponseException var8) {
			GoogleJsonError.ErrorInfo info = (GoogleJsonError.ErrorInfo) var8.getDetails().getErrors().get(0);
			if (!"Authorization".equals(info.getLocation()) || !"forbidden".equals(info.getReason())) {
				throw var8;
			}

			var8.printStackTrace();
		}

	}

	private void postUpload(File processedFile) {
		if (processedFile != this.videoFile) {
			FileUtils.deleteQuietly(processedFile);
		}

	}

	public double getProgress() {
		return (Double) this.progress.get();
	}

	public YoutubeUploader.State getState() {
		return this.state;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public static enum State {
		AUTH, PREPARE_VIDEO, UPLOAD, CLEANUP;

		// $FF: synthetic method
		private static YoutubeUploader.State[] $values() {
			return new YoutubeUploader.State[] { AUTH, PREPARE_VIDEO, UPLOAD, CLEANUP };
		}
	}
}

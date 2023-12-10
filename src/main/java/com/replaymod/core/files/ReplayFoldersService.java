package com.replaymod.core.files;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.net.PercentEscaper;
import com.replaymod.core.Setting;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.utils.Utils;

import net.minecraft.client.Minecraft;

public class ReplayFoldersService {
	private final Path mcDir;
	private final SettingsRegistry settings;
	private static final PercentEscaper CACHE_FILE_NAME_ENCODER = new PercentEscaper("-_ ", false);

	public ReplayFoldersService(SettingsRegistry settings) {
		this.mcDir = Minecraft.getInstance().gameDirectory.toPath();
		this.settings = settings;
	}

	public Path getReplayFolder() throws IOException {
		return Utils.ensureDirectoryExists(this.mcDir.resolve((String) this.settings.get(Setting.RECORDING_PATH)));
	}

	public Path getRawReplayFolder() throws IOException {
		return Utils.ensureDirectoryExists(this.getReplayFolder().resolve("raw"));
	}

	public Path getRecordingFolder() throws IOException {
		return Utils.ensureDirectoryExists(this.getReplayFolder().resolve("recording"));
	}

	public Path getCacheFolder() throws IOException {
		Path path = Utils.ensureDirectoryExists(this.mcDir.resolve((String) this.settings.get(Setting.CACHE_PATH)));

		try {
			Files.setAttribute(path, "dos:hidden", true);
		} catch (UnsupportedOperationException var3) {
		} catch (Exception var4) {
			var4.printStackTrace();
		}

		return path;
	}

	public Path getCachePathForReplay(Path replay) throws IOException {
		Path replayFolder = this.getReplayFolder();
		Path cacheFolder = this.getCacheFolder();
		Path relative = replayFolder.toAbsolutePath().relativize(replay.toAbsolutePath());
		return cacheFolder.resolve(CACHE_FILE_NAME_ENCODER.escape(relative.toString()));
	}

	public Path getReplayPathForCache(Path cache) throws IOException {
		String relative = URLDecoder.decode(cache.getFileName().toString(), "UTF-8");
		Path replayFolder = this.getReplayFolder();
		return replayFolder.resolve(relative);
	}
}

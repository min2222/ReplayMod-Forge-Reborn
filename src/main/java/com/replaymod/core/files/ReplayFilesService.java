package com.replaymod.core.files;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.gui.RestoreReplayGui;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.studio.ReplayStudio;

public class ReplayFilesService {
	private final ReplayFoldersService folders;
	private final Set<Path> lockedPaths = Collections.newSetFromMap(new ConcurrentHashMap());

	public ReplayFilesService(ReplayFoldersService folders) {
		this.folders = folders;
	}

	public ReplayFile open(Path path) throws IOException {
		return this.open(path, path);
	}

	public ReplayFile open(Path input, Path output) throws IOException {
		Path realInput = input != null ? input.toAbsolutePath().normalize() : null;
		Path realOutput = output.toAbsolutePath().normalize();
		if (realInput != null && !this.lockedPaths.add(realInput)) {
			throw new ReplayFilesService.FileLockedException(realInput);
		} else if (!Objects.equals(realInput, realOutput) && !this.lockedPaths.add(realOutput)) {
			if (realInput != null) {
				this.lockedPaths.remove(realInput);
			}

			throw new ReplayFilesService.FileLockedException(realOutput);
		} else {
			Runnable onClose = () -> {
				if (realInput != null) {
					this.lockedPaths.remove(realInput);
				}

				this.lockedPaths.remove(realOutput);
			};

			ZipReplayFile replayFile;
			try {
				replayFile = new ZipReplayFile(new ReplayStudio(), realInput != null ? realInput.toFile() : null,
						realOutput.toFile(), this.folders.getCachePathForReplay(realOutput).toFile());
			} catch (IOException var8) {
				onClose.run();
				throw var8;
			}

			return new ManagedReplayFile(replayFile, onClose);
		}
	}

	public void initialScan(ReplayMod core) {
		DirectoryStream paths;
		Iterator var3;
		Path path;
		try {
			paths = Files.newDirectoryStream(this.folders.getRecordingFolder());

			try {
				var3 = paths.iterator();

				while (var3.hasNext()) {
					path = (Path) var3.next();
					Path destination = this.folders.getReplayFolder().resolve(path.getFileName());
					if (!Files.exists(destination, new LinkOption[0])) {
						Files.move(path, destination);
					}
				}
			} catch (Throwable var12) {
				if (paths != null) {
					try {
						paths.close();
					} catch (Throwable var9) {
						var12.addSuppressed(var9);
					}
				}

				throw var12;
			}

			if (paths != null) {
				paths.close();
			}
		} catch (IOException var13) {
			var13.printStackTrace();
		}

		try {
			paths = Files.newDirectoryStream(this.folders.getReplayFolder());

			try {
				var3 = paths.iterator();

				while (var3.hasNext()) {
					path = (Path) var3.next();
					String name = path.getFileName().toString();
					if (name.endsWith(".mcpr.tmp") && Files.isDirectory(path, new LinkOption[0])) {
						Path original = path.resolveSibling(FilenameUtils.getBaseName(name));
						Path noRecoverMarker = original.resolveSibling(original.getFileName() + ".no_recover");
						if (Files.exists(noRecoverMarker, new LinkOption[0])) {
							FileUtils.deleteDirectory(path.toFile());
							Files.delete(noRecoverMarker);
						} else {
							(new RestoreReplayGui(core, GuiScreen.wrap(core.getMinecraft().screen), original.toFile()))
									.display();
						}
					}
				}
			} catch (Throwable var10) {
				if (paths != null) {
					try {
						paths.close();
					} catch (Throwable var8) {
						var10.addSuppressed(var8);
					}
				}

				throw var10;
			}

			if (paths != null) {
				paths.close();
			}
		} catch (IOException var11) {
			var11.printStackTrace();
		}

		(new Thread(this::cleanup, "replaymod-cleanup")).start();
	}

	private void cleanup() {
		long var1 = 86400000L;

		try {
			Files.walkFileTree(this.folders.getReplayFolder(), new SimpleFileVisitor<Path>() {
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					String name = dir.getFileName().toString();
					if (name.endsWith(".mcpr.cache")) {
						FileUtils.deleteDirectory(dir.toFile());
						return FileVisitResult.SKIP_SUBTREE;
					} else {
						return super.preVisitDirectory(dir, attrs);
					}
				}
			});
		} catch (IOException var13) {
			var13.printStackTrace();
		}

		DirectoryStream paths;
		Iterator var4;
		Path path;
		try {
			paths = Files.newDirectoryStream(this.folders.getRawReplayFolder());

			try {
				var4 = paths.iterator();

				while (var4.hasNext()) {
					path = (Path) var4.next();
					if (Files.getLastModifiedTime(path).toMillis() + 1814400000L < System.currentTimeMillis()) {
						Files.delete(path);
					}
				}
			} catch (Throwable var20) {
				if (paths != null) {
					try {
						paths.close();
					} catch (Throwable var12) {
						var20.addSuppressed(var12);
					}
				}

				throw var20;
			}

			if (paths != null) {
				paths.close();
			}
		} catch (IOException var21) {
			var21.printStackTrace();
		}

		long lastModified;
		try {
			paths = Files.newDirectoryStream(this.folders.getCacheFolder());

			try {
				var4 = paths.iterator();

				label147: while (true) {
					Path replay;
					do {
						do {
							if (!var4.hasNext()) {
								break label147;
							}

							path = (Path) var4.next();
						} while (!Files.isDirectory(path, new LinkOption[0]));

						replay = this.folders.getReplayPathForCache(path);
						lastModified = Files.getLastModifiedTime(path).toMillis();
					} while (lastModified + 604800000L >= System.currentTimeMillis()
							&& Files.exists(replay, new LinkOption[0]));

					FileUtils.deleteDirectory(path.toFile());
				}
			} catch (Throwable var18) {
				if (paths != null) {
					try {
						paths.close();
					} catch (Throwable var11) {
						var18.addSuppressed(var11);
					}
				}

				throw var18;
			}

			if (paths != null) {
				paths.close();
			}
		} catch (IOException var19) {
			var19.printStackTrace();
		}

		String name;
		try {
			paths = Files.newDirectoryStream(this.folders.getReplayFolder());

			try {
				var4 = paths.iterator();

				while (var4.hasNext()) {
					path = (Path) var4.next();
					name = path.getFileName().toString();
					if (name.endsWith(".mcpr.del") && Files.isDirectory(path, new LinkOption[0])) {
						lastModified = Files.getLastModifiedTime(path).toMillis();
						if (lastModified + 172800000L < System.currentTimeMillis()) {
							FileUtils.deleteDirectory(path.toFile());
						}
					}
				}
			} catch (Throwable var16) {
				if (paths != null) {
					try {
						paths.close();
					} catch (Throwable var10) {
						var16.addSuppressed(var10);
					}
				}

				throw var16;
			}

			if (paths != null) {
				paths.close();
			}
		} catch (IOException var17) {
			var17.printStackTrace();
		}

		try {
			paths = Files.newDirectoryStream(this.folders.getReplayFolder());

			try {
				var4 = paths.iterator();

				while (var4.hasNext()) {
					path = (Path) var4.next();
					name = path.getFileName().toString();
					if (name.endsWith(".no_recover")) {
						Files.delete(path);
					}
				}
			} catch (Throwable var14) {
				if (paths != null) {
					try {
						paths.close();
					} catch (Throwable var9) {
						var14.addSuppressed(var9);
					}
				}

				throw var14;
			}

			if (paths != null) {
				paths.close();
			}
		} catch (IOException var15) {
			var15.printStackTrace();
		}

	}

	public static class FileLockedException extends IOException {
		public FileLockedException(Path path) {
			super(path.toString());
		}
	}
}

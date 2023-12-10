package com.replaymod.recording.packet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.replaystudio.replay.ReplayFile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket.Action;

public class ResourcePackRecorder {
	private static final Logger logger = LogManager.getLogger();
	private static final Minecraft mc = MCVer.getMinecraft();
	private final ReplayFile replayFile;
	private int nextRequestId;

	public ResourcePackRecorder(ReplayFile replayFile) {
		this.replayFile = replayFile;
	}

	public void recordResourcePack(File file, int requestId) {
		try {
			byte[] bytes = Files.toByteArray(file);
			String hash = Hashing.sha1().hashBytes(bytes).toString();
			boolean doWrite = false;
			synchronized (this.replayFile) {
				Map<Integer, String> index = this.replayFile.getResourcePackIndex();
				if (index == null) {
					index = new HashMap();
				}

				if (!((Map) index).containsValue(hash)) {
					doWrite = true;
				}

				((Map) index).put(requestId, hash);
				this.replayFile.writeResourcePackIndex((Map) index);
			}

			if (doWrite) {
				OutputStream out = this.replayFile.writeResourcePack(hash);

				try {
					out.write(bytes);
				} catch (Throwable var10) {
					if (out != null) {
						try {
							out.close();
						} catch (Throwable var9) {
							var10.addSuppressed(var9);
						}
					}

					throw var10;
				}

				if (out != null) {
					out.close();
				}
			}
		} catch (IOException var12) {
			logger.warn("Failed to save resource pack.", var12);
		}

	}

	public ServerboundResourcePackPacket makeStatusPacket(String hash, Action action) {
		return new ServerboundResourcePackPacket(action);
	}

	public synchronized ClientboundResourcePackPacket handleResourcePack(Connection netManager,
			ClientboundResourcePackPacket packet) {
		int requestId = this.nextRequestId++;
		String url = packet.getUrl();
		String hash = packet.getHash();
		if (url.startsWith("level://")) {
			String levelName = url.substring("level://".length());
			File savesDir = new File(mc.gameDirectory, "saves");
			File levelDir = new File(savesDir, levelName);
			if (levelDir.isFile()) {
				netManager.send(this.makeStatusPacket(hash, Action.ACCEPTED));
				MCVer.addCallback(MCVer.setServerResourcePack(levelDir), (result) -> {
					this.recordResourcePack(levelDir, requestId);
					netManager.send(this.makeStatusPacket(hash, Action.SUCCESSFULLY_LOADED));
				}, (throwable) -> {
					netManager.send(this.makeStatusPacket(hash, Action.FAILED_DOWNLOAD));
				});
			} else {
				netManager.send(this.makeStatusPacket(hash, Action.FAILED_DOWNLOAD));
			}
		} else {
			ServerData serverData = mc.getCurrentServer();
			if (serverData != null && serverData.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
				netManager.send(this.makeStatusPacket(hash, Action.ACCEPTED));
				this.downloadResourcePackFuture(netManager, requestId, url, hash);
			} else if (serverData != null && serverData.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT) {
				netManager.send(this.makeStatusPacket(hash, Action.DECLINED));
			} else {
				mc.execute(() -> {
					mc.setScreen(new ConfirmScreen((result) -> {
						if (serverData != null) {
							serverData.setResourcePackStatus(result ? ServerData.ServerPackStatus.ENABLED
									: ServerData.ServerPackStatus.DISABLED);
						}

						if (result) {
							netManager.send(this.makeStatusPacket(hash, Action.ACCEPTED));
							this.downloadResourcePackFuture(netManager, requestId, url, hash);
						} else {
							netManager.send(this.makeStatusPacket(hash, Action.DECLINED));
						}

						ServerList.saveSingleServer(serverData);
						mc.setScreen((Screen) null);
					}, Component.translatable("multiplayer.texturePrompt.line1"),
							Component.translatable("multiplayer.texturePrompt.line2")));
				});
			}
		}

		return new ClientboundResourcePackPacket("replay://" + requestId, "", packet.isRequired(), packet.getPrompt());
	}

	private void downloadResourcePackFuture(Connection connection, int requestId, String url, String hash) {
		MCVer.addCallback(this.downloadResourcePack(requestId, url, hash), (result) -> {
			connection.send(this.makeStatusPacket(hash, Action.SUCCESSFULLY_LOADED));
		}, (throwable) -> {
			connection.send(this.makeStatusPacket(hash, Action.FAILED_DOWNLOAD));
		});
	}

	private CompletableFuture<?> downloadResourcePack(int requestId, String url, String hash) {
		ClientPackSource packFinder = mc.getClientPackSource();
		((ResourcePackRecorder.IDownloadingPackFinder) packFinder).setRequestCallback((file) -> {
			this.recordResourcePack(file, requestId);
		});

		try {
			URL theUrl = new URL(url);
			String protocol = theUrl.getProtocol();
			if (!"http".equals(protocol) && !"https".equals(protocol)) {
				throw new MalformedURLException("Unsupported protocol.");
			} else {
				return packFinder.downloadAndSelectResourcePack(theUrl, hash, true);
			}
		} catch (MalformedURLException var7) {
			return CompletableFuture.failedFuture(var7);
		}
	}

	public interface IDownloadingPackFinder {
		void setRequestCallback(Consumer<File> consumer);
	}
}

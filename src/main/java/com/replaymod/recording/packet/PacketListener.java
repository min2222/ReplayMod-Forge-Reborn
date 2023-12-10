package com.replaymod.recording.packet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.PooledByteBufAllocator;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import com.google.gson.Gson;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Restrictions;
import com.replaymod.core.versions.MCVer;
import com.replaymod.editor.gui.MarkerProcessor;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.recording.Setting;
import com.replaymod.recording.gui.GuiSavingReplay;
import com.replaymod.recording.handler.ConnectionEventHandler;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.io.ReplayOutputStream;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.packet.State;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import com.replaymod.replaystudio.util.Utils;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.world.entity.Entity;

@Sharable
public class PacketListener extends ChannelInboundHandlerAdapter {
	public static final String RAW_RECORDER_KEY = "replay_recorder_raw";
	public static final String DECODED_RECORDER_KEY = "replay_recorder_decoded";
	public static final String DECOMPRESS_KEY = "decompress";
	public static final String DECODER_KEY = "decoder";
	private static final Minecraft mc = MCVer.getMinecraft();
	private static final Logger logger = LogManager.getLogger();
	private static final int PACKET_ID_RESOURCE_PACK_SEND;
	private static final int PACKET_ID_LOGIN_COMPRESSION;
	private final ReplayMod core;
	private final Path outputPath;
	private final ReplayFile replayFile;
	private final ResourcePackRecorder resourcePackRecorder;
	private final ExecutorService saveService = Executors.newSingleThreadExecutor();
	private final ReplayOutputStream packetOutputStream;
	private ReplayMetaData metaData;
	private ChannelHandlerContext context = null;
	private final long startTime;
	private long lastSentPacket;
	private long timePassedWhilePaused;
	private volatile boolean serverWasPaused;
	private final AtomicInteger lastSaveMetaDataId = new AtomicInteger();

	public PacketListener(ReplayMod core, Path outputPath, ReplayFile replayFile, ReplayMetaData metaData)
			throws IOException {
		this.core = core;
		this.outputPath = outputPath;
		this.replayFile = replayFile;
		this.metaData = metaData;
		this.resourcePackRecorder = new ResourcePackRecorder(replayFile);
		this.packetOutputStream = replayFile.writePacketData();
		this.startTime = metaData.getDate();
		this.saveMetaData();
	}

	private void saveMetaData() {
		int id = this.lastSaveMetaDataId.incrementAndGet();
		this.saveService.submit(() -> {
			if (this.lastSaveMetaDataId.get() == id) {
				try {
					synchronized (this.replayFile) {
						if (ReplayMod.isMinimalMode()) {
							this.metaData.setFileFormat("MCPR");
							this.metaData.setFileFormatVersion(14);
							this.metaData.setProtocolVersion(MCVer.getProtocolVersion());
							this.metaData.setGenerator("ReplayMod in Minimal Mode");
							OutputStream out = this.replayFile.write("metaData.json");

							try {
								String json = (new Gson()).toJson(this.metaData);
								out.write(json.getBytes());
							} catch (Throwable var8) {
								if (out != null) {
									try {
										out.close();
									} catch (Throwable var7) {
										var8.addSuppressed(var7);
									}
								}

								throw var8;
							}

							if (out != null) {
								out.close();
							}
						} else {
							this.replayFile.writeMetaData(MCVer.getPacketTypeRegistry(true), this.metaData);
						}
					}
				} catch (IOException var10) {
					logger.error("Writing metadata:", var10);
				}

			}
		});
	}

	public void save(Packet packet) {
		com.replaymod.replaystudio.protocol.Packet encoded;
		try {
			encoded = encodeMcPacket(this.getConnectionState(), packet);
		} catch (Exception var4) {
			logger.error("Encoding packet:", var4);
			return;
		}

		this.save(encoded);
	}

	public void save(com.replaymod.replaystudio.protocol.Packet packet) {
		if (!mc.isSameThread()) {
			mc.tell(() -> {
				this.save(packet);
			});
		} else {
			try {
				if (packet.getRegistry().getState() == State.LOGIN && packet.getId() == PACKET_ID_LOGIN_COMPRESSION) {
					return;
				}

				long now = System.currentTimeMillis();
				if (this.serverWasPaused) {
					this.timePassedWhilePaused = now - this.startTime - this.lastSentPacket;
					this.serverWasPaused = false;
				}

				int timestamp = (int) (now - this.startTime - this.timePassedWhilePaused);
				this.lastSentPacket = (long) timestamp;
				PacketData packetData = new PacketData((long) timestamp, packet);
				this.saveService.submit(() -> {
					try {
						if (ReplayMod.isMinimalMode()) {
							ByteBuf packetIdBuf = PooledByteBufAllocator.DEFAULT.buffer();
							ByteBuf packetBuf = packetData.getPacket().getBuf();

							try {
								(new ByteBufNetOutput(packetIdBuf)).writeVarInt(packetData.getPacket().getId());
								int packetIdLen = packetIdBuf.readableBytes();
								int packetBufLen = packetBuf.readableBytes();
								Utils.writeInt(this.packetOutputStream, (int) packetData.getTime());
								Utils.writeInt(this.packetOutputStream, packetIdLen + packetBufLen);
								packetIdBuf.readBytes(this.packetOutputStream, packetIdLen);
								packetBuf.getBytes(packetBuf.readerIndex(), this.packetOutputStream, packetBufLen);
							} finally {
								packetIdBuf.release();
								packetBuf.release();
							}
						} else {
							this.packetOutputStream.write(packetData);
						}

					} catch (IOException var10) {
						throw new RuntimeException(var10);
					}
				});
			} catch (Exception var6) {
				logger.error("Writing packet:", var6);
			}

		}
	}

	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		if (ctx.pipeline().get("replay_recorder_decoded") == null) {
			if (ctx.pipeline().get("decoder") != null) {
				ctx.pipeline().addAfter("decoder", "replay_recorder_decoded",
						new PacketListener.DecodedPacketListener());
			} else {
				ctx.pipeline().addAfter("replay_recorder_raw", "replay_recorder_decoded",
						new PacketListener.DecodedPacketListener());
			}
		}

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		metaData.setDuration((int) lastSentPacket);
		saveMetaData();

		core.runLater(() -> {
			ConnectionEventHandler connectionEventHandler = ReplayModRecording.instance.getConnectionEventHandler();
			if (connectionEventHandler.getPacketListener() == this) {
				connectionEventHandler.reset();
			}
		});

		GuiSavingReplay guiSavingReplay = new GuiSavingReplay(core);
		new Thread(() -> {
			core.runLater(guiSavingReplay::open);

			saveService.shutdown();
			try {
				saveService.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("Waiting for save service termination:", e);
			}
			try {
				packetOutputStream.close();
			} catch (IOException e) {
				logger.error("Failed to close packet output stream:", e);
			}

			List<Pair<Path, ReplayMetaData>> outputPaths;
			synchronized (replayFile) {
				try {
					if (!MarkerProcessor.producesAnyOutput(replayFile)) {
						// Immediately close the saving popup, the user doesn't care about it
						core.runLater(guiSavingReplay::close);

						// If we crash right here, on the next start we'll prompt the user for recovery
						// but we don't really want that, so drop a marker file to skip recovery for
						// this replay.
						Path noRecoverMarker = outputPath.resolveSibling(outputPath.getFileName() + ".no_recover");
						Files.createFile(noRecoverMarker);

						// We still have the replay, so we just save it (at least for a few weeks) in
						// case they change their mind
						String replayName = FilenameUtils.getBaseName(outputPath.getFileName().toString());
						Path rawFolder = ReplayMod.instance.folders.getRawReplayFolder();
						Path rawPath = rawFolder.resolve(outputPath.getFileName());
						for (int i = 1; Files.exists(rawPath); i++) {
							rawPath = rawPath.resolveSibling(replayName + "." + i + ".mcpr");
						}
						replayFile.saveTo(rawPath.toFile());
						replayFile.close();

						// Done, clean up the marker
						Files.delete(noRecoverMarker);
						return;
					}

					replayFile.save();
					replayFile.close();

					if (core.getSettingsRegistry().get(Setting.AUTO_POST_PROCESS) && !ReplayMod.isMinimalMode()) {
						outputPaths = MarkerProcessor.apply(outputPath, guiSavingReplay.getProgressBar()::setProgress);
					} else {
						outputPaths = Collections.singletonList(Pair.of(outputPath, metaData));
					}
				} catch (Exception e) {
					logger.error("Saving replay file:", e);
					CrashReport crashReport = CrashReport.forThrowable(e, "Saving replay file");
					core.runLater(() -> com.replaymod.core.utils.Utils.error(logger, VanillaGuiScreen.wrap(mc.screen),
							crashReport, guiSavingReplay::close));
					return;
				}
			}

			core.runLater(() -> guiSavingReplay.presentRenameDialog(outputPaths));
		}).start();
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (ctx == null) {
			if (this.context == null) {
				return;
			}

			ctx = this.context;
		}

		this.context = ctx;
		ConnectionProtocol connectionState = this.getConnectionState();
		com.replaymod.replaystudio.protocol.Packet packet = null;
		if (msg instanceof ByteBuf) {
			io.netty.buffer.ByteBuf buf = (io.netty.buffer.ByteBuf) msg;
			if (buf.readableBytes() > 0) {
				packet = decodePacket(connectionState, buf);
			}
		} else if (msg instanceof Packet) {
			packet = encodeMcPacket(connectionState, (Packet) msg);
		}

		if (packet != null) {
			if (connectionState == ConnectionProtocol.PLAY && packet.getId() == PACKET_ID_RESOURCE_PACK_SEND) {
				Connection connection = ctx.pipeline().get(Connection.class);
				this.save((Packet) this.resourcePackRecorder.handleResourcePack(connection,
						(ClientboundResourcePackPacket) decodeMcPacket(packet)));
				return;
			}

			this.save(packet);
		}

		super.channelRead(ctx, msg);
	}

	private ConnectionProtocol getConnectionState() {
		ChannelHandlerContext ctx = this.context;
		if (ctx == null) {
			return ConnectionProtocol.LOGIN;
		} else {
			AttributeKey<ConnectionProtocol> key = Connection.ATTRIBUTE_PROTOCOL;
			return ctx.channel().attr(key).get();
		}
	}

	private static com.replaymod.replaystudio.protocol.Packet encodeMcPacket(ConnectionProtocol connectionState,
			Packet packet) throws Exception {
		Integer packetId = connectionState.getPacketId(PacketFlow.CLIENTBOUND, packet);
		if (packetId == null) {
			throw new IOException("Unknown packet type:" + packet.getClass());
		} else {
			io.netty.buffer.ByteBuf byteBuf = Unpooled.buffer();

			com.replaymod.replaystudio.protocol.Packet var4;
			try {
				packet.write(new FriendlyByteBuf(byteBuf));
				var4 = new com.replaymod.replaystudio.protocol.Packet(
						MCVer.getPacketTypeRegistry(connectionState == ConnectionProtocol.LOGIN), packetId,
						com.github.steveice10.netty.buffer.Unpooled.wrappedBuffer(byteBuf.array(),
								byteBuf.arrayOffset(), byteBuf.readableBytes()));
			} finally {
				byteBuf.release();
			}

			return var4;
		}
	}

	private static Packet decodeMcPacket(com.replaymod.replaystudio.protocol.Packet packet)
			throws IOException, IllegalAccessException, InstantiationException {
		ConnectionProtocol connectionState = packet.getRegistry().getState() == State.LOGIN ? ConnectionProtocol.LOGIN
				: ConnectionProtocol.PLAY;
		int packetId = packet.getId();
		FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.getBuf().nioBuffer()));
		return connectionState.createPacket(PacketFlow.CLIENTBOUND, packetId, packetBuf);
	}

	private static com.replaymod.replaystudio.protocol.Packet decodePacket(ConnectionProtocol connectionState,
			io.netty.buffer.ByteBuf buf) {
		FriendlyByteBuf packetBuf = new FriendlyByteBuf(buf.slice());
		int packetId = packetBuf.readVarInt();
		byte[] bytes = new byte[packetBuf.readableBytes()];
		packetBuf.readBytes(bytes);
		return new com.replaymod.replaystudio.protocol.Packet(
				MCVer.getPacketTypeRegistry(connectionState == ConnectionProtocol.LOGIN), packetId,
				com.github.steveice10.netty.buffer.Unpooled.wrappedBuffer(bytes));
	}

	private static int getPacketId(ConnectionProtocol networkState, Packet packet) {
		try {
			return (Integer) Objects.requireNonNull(networkState.getPacketId(PacketFlow.CLIENTBOUND, packet));
		} catch (Exception var3) {
			throw new RuntimeException("Failed to determine packet id for " + packet.getClass(), var3);
		}
	}

	public void addMarker(String name) {
		this.addMarker(name, (int) this.getCurrentDuration());
	}

	public void addMarker(String name, int timestamp) {
		Entity view = mc.getCameraEntity();
		Marker marker = new Marker();
		marker.setName(name);
		marker.setTime(timestamp);
		if (view != null) {
			marker.setX(view.getX());
			marker.setY(view.getY());
			marker.setZ(view.getZ());
			marker.setYaw(view.getYRot());
			marker.setPitch(view.getXRot());
		}

		this.saveService.submit(() -> {
			synchronized (this.replayFile) {
				try {
					Set<Marker> markers = (Set) this.replayFile.getMarkers().or(HashSet::new);
					markers.add(marker);
					this.replayFile.writeMarkers(markers);
				} catch (IOException var5) {
					logger.error("Writing markers:", var5);
				}

			}
		});
	}

	public long getCurrentDuration() {
		return this.lastSentPacket;
	}

	public void setServerWasPaused() {
		this.serverWasPaused = true;
	}

	static {
		PACKET_ID_RESOURCE_PACK_SEND = getPacketId(ConnectionProtocol.PLAY,
				new ClientboundResourcePackPacket("", "", false, null));
		PACKET_ID_LOGIN_COMPRESSION = getPacketId(ConnectionProtocol.LOGIN, new ClientboundLoginCompressionPacket(0));
	}

	private class DecodedPacketListener extends ChannelInboundHandlerAdapter {
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof ClientboundCustomPayloadPacket) {
				ClientboundCustomPayloadPacket packet = (ClientboundCustomPayloadPacket) msg;
				if (Restrictions.PLUGIN_CHANNEL.equals(packet.getName())) {
					PacketListener.this.save((Packet) (new ClientboundDisconnectPacket(
							Component.literal("Please update to view this replay."))));
				}
			}

			if (msg instanceof ClientboundAddPlayerPacket) {
				UUID uuid = ((ClientboundAddPlayerPacket) msg).getPlayerId();
				Set<String> uuids = new HashSet(Arrays.asList(PacketListener.this.metaData.getPlayers()));
				uuids.add(uuid.toString());
				PacketListener.this.metaData.setPlayers((String[]) uuids.toArray(new String[uuids.size()]));
				PacketListener.this.saveMetaData();
			}

			super.channelRead(ctx, msg);
		}
	}
}

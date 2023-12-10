package com.replaymod.replay;

import static com.replaymod.core.versions.MCVer.getMinecraft;
import static com.replaymod.core.versions.MCVer.getPacketTypeRegistry;
import static com.replaymod.core.versions.MCVer.setServerResourcePack;
import static com.replaymod.replaystudio.util.Utils.readInt;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Restrictions;
import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.core.versions.MCVer.MinecraftMethodAccessor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.replay.camera.CameraEntity;
import com.replaymod.replay.mixin.MinecraftAccessor;
import com.replaymod.replay.mixin.TimerAccessor;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.replay.ReplayFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;

/**
 * Sends replay packets to netty channels. Even though {@link Sharable}, this
 * should never be added to multiple pipes at once, it may however be re-added
 * when the replay restart from the beginning.
 */
@Sharable
public class FullReplaySender extends ChannelDuplexHandler implements ReplaySender {
	/**
	 * These packets are ignored completely during replay.
	 */
	private static final List<Class<?>> BAD_PACKETS = Arrays.<Class<?>>asList(
			// TODO
			ServerboundPlayerActionPacket.class, ClientboundOpenBookPacket.class, ClientboundOpenScreenPacket.class,
			ClientboundUpdateRecipesPacket.class, ClientboundUpdateAdvancementsPacket.class,
			ClientboundSelectAdvancementsTabPacket.class, ClientboundSetCameraPacket.class,
			ClientboundSetTitlesAnimationPacket.class, ClientboundSetHealthPacket.class,
			ClientboundHorseScreenOpenPacket.class, ClientboundContainerClosePacket.class,
			ClientboundContainerSetSlotPacket.class, ClientboundContainerSetDataPacket.class,
			ClientboundOpenSignEditorPacket.class, ClientboundAwardStatsPacket.class,
			ClientboundAddExperienceOrbPacket.class, ClientboundPlayerAbilitiesPacket.class);

	private static int TP_DISTANCE_LIMIT = 128;

	/**
	 * The replay handler responsible for the current replay.
	 */
	private final ReplayHandler replayHandler;

	/**
	 * Whether to work in async mode.
	 * <p>
	 * When in async mode, a separate thread send packets and waits according to
	 * their delays. This is default in normal playback mode.
	 * <p>
	 * When in sync mode, no packets will be sent until
	 * {@link #sendPacketsTill(int)} is called. This is used during path playback
	 * and video rendering.
	 */
	protected boolean asyncMode;

	/**
	 * Timestamp of the last packet sent in milliseconds since the start.
	 */
	protected int lastTimeStamp;

	/**
	 * @see #currentTimeStamp()
	 */
	protected int currentTimeStamp;

	/**
	 * The replay file.
	 */
	protected ReplayFile replayFile;

	/**
	 * The channel handler context used to send packets to minecraft.
	 */
	protected ChannelHandlerContext ctx;

	/**
	 * The replay input stream from which new packets are read. When accessing this
	 * stream make sure to synchronize on {@code this} as it's used from multiple
	 * threads.
	 */
	protected ReplayInputStream replayIn;

	/**
	 * The next packet that should be sent. This is required as some actions such as
	 * jumping to a specified timestamp have to peek at the next packet.
	 */
	protected PacketData nextPacket;

	/**
	 * Whether we're currently reading packets from the login phase.
	 */
	private boolean loginPhase = true;

	/**
	 * Whether we need to restart the current replay. E.g. when jumping backwards in
	 * time
	 */
	protected boolean startFromBeginning = true;

	/**
	 * Whether to terminate the replay. This only has an effect on the async mode
	 * and is {@code true} during sync mode.
	 */
	protected boolean terminate;

	/**
	 * The speed of the replay. 1 is normal, 2 is twice as fast, 0.5 is half speed
	 * and 0 is frozen
	 */
	protected double replaySpeed = 1f;

	/**
	 * Whether the world has been loaded and the dirt-screen should go away.
	 */
	protected boolean hasWorldLoaded;

	/**
	 * The minecraft instance.
	 */
	protected Minecraft mc = getMinecraft();

	/**
	 * The total length of this replay in milliseconds.
	 */
	protected final int replayLength;

	/**
	 * Our actual entity id that the server gave to us.
	 */
	protected int actualID = -1;

	/**
	 * Whether to allow (process) the next player movement packet.
	 */
	protected boolean allowMovement;

	/**
	 * Directory to which resource packs are extracted.
	 */
	private final File tempResourcePackFolder = Files.createTempDir();

	private final EventHandler events = new EventHandler();

	/**
	 * Create a new replay sender.
	 *
	 * @param file      The replay file
	 * @param asyncMode {@code true} for async mode, {@code false} otherwise
	 * @see #asyncMode
	 */
	public FullReplaySender(ReplayHandler replayHandler, ReplayFile file, boolean asyncMode) throws IOException {
		this.replayHandler = replayHandler;
		this.replayFile = file;
		this.asyncMode = asyncMode;
		this.replayLength = file.getMetaData().getDuration();

		events.register();

		if (asyncMode) {
			new Thread(asyncSender, "replaymod-async-sender").start();
		}
	}

	/**
	 * Set whether this replay sender operates in async mode. When in async mode, it
	 * will send packets timed from a separate thread. When not in async mode, it
	 * will send packets when {@link #sendPacketsTill(int)} is called.
	 *
	 * @param asyncMode {@code true} to enable async mode
	 */
	@Override
	public void setAsyncMode(boolean asyncMode) {
		if (this.asyncMode == asyncMode)
			return;
		this.asyncMode = asyncMode;
		if (asyncMode) {
			this.terminate = false;
			new Thread(asyncSender, "replaymod-async-sender").start();
		} else {
			this.terminate = true;
		}
	}

	@Override
	public boolean isAsyncMode() {
		return asyncMode;
	}

	/**
	 * Set whether this replay sender to operate in sync mode. When in sync mode, it
	 * will send packets when {@link #sendPacketsTill(int)} is called. This call
	 * will block until the async worker thread has stopped.
	 */
	@Override
	public void setSyncModeAndWait() {
		if (!this.asyncMode)
			return;
		this.asyncMode = false;
		this.terminate = true;
		synchronized (this) {
			// This will wait for the worker thread to leave the synchronized code part
		}
	}

	/**
	 * Return a fake system tile in milliseconds value that respects
	 * slowdown/speedup/pause and works in both, sync and async mode. Note: For sync
	 * mode this returns the last value passed to {@link #sendPacketsTill(int)}.
	 *
	 * @return The timestamp in milliseconds since the start of the replay
	 */
	@Override
	public int currentTimeStamp() {
		if (asyncMode && !paused()) {
			return (int) ((System.currentTimeMillis() - realTimeStart) * realTimeStartSpeed);
		} else {
			return lastTimeStamp;
		}
	}

	/**
	 * Terminate this replay sender.
	 */
	public void terminateReplay() {
		if (terminate) {
			return;
		}
		terminate = true;
		syncSender.shutdown();
		events.unregister();
		try {
			channelInactive(ctx);
			ctx.channel().pipeline().close();
			FileUtils.deleteDirectory(tempResourcePackFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class EventHandler extends EventRegistrations {
		{
			on(PreTickCallback.EVENT, this::onWorldTick);
		}

		private void onWorldTick() {
			// Spawning a player into an empty chunk (which we might do with the recording
			// player)
			// prevents it from being moved by teleport packets (it essentially gets stuck)
			// because
			// Entity#addedToChunk is not set and it is therefore not updated every tick.
			// To counteract this, we need to manually update it's position if it hasn't
			// been added
			// to any chunk yet.
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// When in async mode and the replay sender shut down, then don't send packets
		if (terminate && asyncMode) {
			return;
		}

		// When a packet is sent directly, perform no filtering
		if (msg instanceof Packet) {
			super.channelRead(ctx, msg);
		}

		if (msg instanceof byte[]) {
			try {
				Packet p = deserializePacket((byte[]) msg);

				if (p != null) {
					p = processPacket(p);
					if (p != null) {
						super.channelRead(ctx, p);
					}

					maybeRemoveDeadEntities(p);

					if (p instanceof ClientboundLevelChunkPacketData) {
						Runnable doLightUpdates = () -> {
							ClientLevel world = mc.level;
							if (world != null) {
								while (!world.isLightUpdateQueueEmpty())
									world.pollLightUpdates();
								LevelLightEngine provider = world.getChunkSource().getLightEngine();
								while (provider.hasLightWork()) {
									provider.runUpdates(Integer.MAX_VALUE, true, true);
								}
							}
						};
						if (mc.isSameThread()) {
							doLightUpdates.run();
						} else {
							mc.tell(doLightUpdates);
						}
					}
				}
			} catch (Exception e) {
				// We'd rather not have a failure parsing one packet screw up the whole replay
				// process
				e.printStackTrace();
			}
		}

	}

	private void maybeRemoveDeadEntities(Packet<?> packet) {
		if (asyncMode) {
			return; // MC should have enough time to tick
		}

		boolean relevantPacket = packet instanceof ClientboundAddPlayerPacket
				|| packet instanceof ClientboundAddEntityPacket || packet instanceof ClientboundAddExperienceOrbPacket
				|| packet instanceof ClientboundRemoveEntitiesPacket;
		if (!relevantPacket) {
			return; // don't want to do it too often, only when there's likely to be a dead entity
		}

		mc.tell(() -> {
			ClientLevel world = mc.level;
			if (world != null) {
				// removeDeadEntities(world);
			}
		});
	}

	private Packet deserializePacket(byte[] bytes) {
		ByteBuf bb = Unpooled.wrappedBuffer(bytes);
		FriendlyByteBuf pb = new FriendlyByteBuf(bb);
		int i = pb.readVarInt();
		ConnectionProtocol state = loginPhase ? ConnectionProtocol.LOGIN : ConnectionProtocol.PLAY;
		Packet p = state.createPacket(PacketFlow.CLIENTBOUND, i, pb);
		return p;
	}

	/**
	 * Process a packet and return the result.
	 *
	 * @param p The packet to process
	 * @return The processed packet or {@code null} if no packet shall be sent
	 */
	protected Packet processPacket(Packet p) throws Exception {
		if (p instanceof ClientboundGameProfilePacket) {
			loginPhase = false;
			return p;
		}
		if (p instanceof ClientboundCustomPayloadPacket) {
			ClientboundCustomPayloadPacket packet = (ClientboundCustomPayloadPacket) p;
			if (Restrictions.PLUGIN_CHANNEL.equals(packet.getName())) {
				final String unknown = replayHandler.getRestrictions().handle(packet);
				if (unknown == null) {
					return null;
				} else {
					// Failed to parse options, make sure that under no circumstances further
					// packets are parsed
					terminateReplay();
					// Then end replay and show error GUI
					ReplayMod.instance.runLater(() -> {
						try {
							replayHandler.endReplay();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mc.setScreen(new AlertScreen(() -> mc.setScreen(null),
								Component.translatable("replaymod.error.unknownrestriction1"),
								Component.translatable("replaymod.error.unknownrestriction2", unknown)));
					});
				}
			}
		}
		if (p instanceof ClientboundDisconnectPacket) {
			Component reason = ((ClientboundDisconnectPacket) p).getReason();
			String message = reason.getString();
			if ("Please update to view this replay.".equals(message)) {
				// This version of the mod supports replay restrictions so we are allowed
				// to remove this packet.
				return null;
			}
		}

		if (BAD_PACKETS.contains(p.getClass()))
			return null;

		if (p instanceof ClientboundResourcePackPacket) {
			ClientboundResourcePackPacket packet = (ClientboundResourcePackPacket) p;
			String url = packet.getUrl();
			if (url.startsWith("replay://")) {
				int id = Integer.parseInt(url.substring("replay://".length()));
				Map<Integer, String> index = replayFile.getResourcePackIndex();
				if (index != null) {
					String hash = index.get(id);
					if (hash != null) {
						File file = new File(tempResourcePackFolder, hash + ".zip");
						if (!file.exists()) {
							IOUtils.copy(replayFile.getResourcePack(hash).get(), new FileOutputStream(file));
						}
						setServerResourcePack(file);
					}
				}
				return null;
			}
		}

		if (p instanceof ClientboundLoginPacket) {
			ClientboundLoginPacket packet = (ClientboundLoginPacket) p;
			int entId = packet.playerId();
			allowMovement = true;
			actualID = entId;
			entId = -1789435; // Camera entity id should be negative which is an invalid id and can't be used
								// by servers
			p = new ClientboundLoginPacket(entId, packet.hardcore(), GameType.SPECTATOR, GameType.SPECTATOR,
					packet.levels(), packet.registryHolder(), packet.dimensionType(), packet.dimension(), packet.seed(),
					0, packet.chunkRadius(), packet.simulationDistance(), packet.reducedDebugInfo(),
					packet.showDeathScreen(), packet.isDebug(), packet.isFlat(), Optional.empty());
		}

		if (p instanceof ClientboundRespawnPacket) {
			ClientboundRespawnPacket respawn = (ClientboundRespawnPacket) p;
			p = new ClientboundRespawnPacket(respawn.getDimensionType(), respawn.getDimension(), respawn.getSeed(),
					GameType.SPECTATOR, GameType.SPECTATOR, respawn.isDebug(), respawn.isFlat(),
					respawn.shouldKeepAllPlayerData(), respawn.getLastDeathLocation());

			allowMovement = true;
		}

		if (p instanceof ClientboundPlayerPositionPacket) {
			final ClientboundPlayerPositionPacket ppl = (ClientboundPlayerPositionPacket) p;
			if (!hasWorldLoaded)
				hasWorldLoaded = true;

			ReplayMod.instance.runLater(() -> {
				if (mc.screen instanceof ReceivingLevelScreen) {
					// Close the world loading screen manually in case we swallow the packet
					mc.setScreen(null);
				}
			});

			if (replayHandler.shouldSuppressCameraMovements())
				return null;

			CameraEntity cent = replayHandler.getCameraEntity();

			for (ClientboundPlayerPositionPacket.RelativeArgument relative : ppl.getRelativeArguments()) {
				if (relative == ClientboundPlayerPositionPacket.RelativeArgument.X
						|| relative == ClientboundPlayerPositionPacket.RelativeArgument.Y
						|| relative == ClientboundPlayerPositionPacket.RelativeArgument.Z) {
					return null;
				}
			}

			if (cent != null) {
				if (!allowMovement && !((Math.abs(cent.getX() - ppl.getX()) > TP_DISTANCE_LIMIT)
						|| (Math.abs(cent.getZ() - ppl.getZ()) > TP_DISTANCE_LIMIT))) {
					return null;
				} else {
					allowMovement = false;
				}
			}

			new Runnable() {
				@Override
				public void run() {
					if (mc.level == null || !mc.isSameThread()) {
						ReplayMod.instance.runLater(this);
						return;
					}

					CameraEntity cent = replayHandler.getCameraEntity();
					cent.setCameraPosition(ppl.getX(), ppl.getY(), ppl.getZ());
				}
			}.run();
		}

		if (p instanceof ClientboundGameEventPacket) {
			ClientboundGameEventPacket pg = (ClientboundGameEventPacket) p;
			if (!Arrays.asList(ClientboundGameEventPacket.START_RAINING, ClientboundGameEventPacket.STOP_RAINING,
					ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE)
					.contains(pg.getEvent())) {
				return null;
			}
		}
		if (p instanceof ClientboundSystemChatPacket || p instanceof ClientboundPlayerChatPacket
				|| p instanceof ClientboundPlayerChatHeaderPacket) {
			if (!ReplayModReplay.instance.getCore().getSettingsRegistry().get(Setting.SHOW_CHAT)) {
				return null;
			}
		}

		return asyncMode ? processPacketAsync(p) : processPacketSync(p);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
		super.channelActive(ctx);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// The embedded channel's event loop will consider every thread to be in it and
		// as such provides no
		// guarantees that only one thread is using the pipeline at any one time.
		// For reading the replay sender (either sync or async) is the only thread ever
		// writing.
		// For writing it may very well happen that multiple threads want to use the
		// pipline at the same time.
		// It's unclear whether the EmbeddedChannel is supposed to be thread-safe (the
		// behavior of the event loop
		// does suggest that). However it seems like it either isn't (likely) or there
		// is a race condition.
		// See: https://www.replaymod.com/forum/thread/1752#post8045
		// (https://paste.replaymod.com/lotacatuwo)
		// To work around this issue, we just outright drop all write/flush requests
		// (they aren't needed anyway).
		// This still leaves channel handlers upstream with the threading issue but they
		// all seem to cope well with it.
		promise.setSuccess();
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		// See write method above
	}

	/**
	 * Returns the speed of the replay. 1 being normal speed, 0.5 half and 2 twice
	 * as fast. If 0 is returned, the replay is paused.
	 *
	 * @return speed multiplier
	 */
	@Override
	public double getReplaySpeed() {
		if (!paused())
			return replaySpeed;
		else
			return 0;
	}

	/**
	 * Set the speed of the replay. 1 being normal speed, 0.5 half and 2 twice as
	 * fast. The speed may not be set to 0 nor to negative values.
	 *
	 * @param d Speed multiplier
	 */
	@Override
	public void setReplaySpeed(final double d) {
		if (d != 0) {
			this.replaySpeed = d;
			this.realTimeStartSpeed = d;
			this.realTimeStart = System.currentTimeMillis() - (long) (lastTimeStamp / d);
		}
		TimerAccessor timer = (TimerAccessor) ((MinecraftAccessor) mc).getTimer();
		timer.setTickLength(WrappedTimer.DEFAULT_MS_PER_TICK / (float) d);
	}

	/////////////////////////////////////////////////////////
	// Asynchronous packet processing //
	/////////////////////////////////////////////////////////

	/**
	 * Timestamp in milliseconds of when we started (or would have started when
	 * taking pauses and speed into account) the playback of the replay. Updated
	 * only when replay speed changes or on pause/unpause but definitely not on
	 * every packet to prevent gradual drifting.
	 */
	private long realTimeStart;

	/**
	 * The replay speed used for {@link #realTimeStart}. If the target speed differs
	 * from this one, the timestamp is recalculated.
	 */
	private double realTimeStartSpeed;

	/**
	 * There is no waiting performed until a packet with at least this timestamp is
	 * reached (but not yet sent). If this is -1, then timing is normal.
	 */
	private long desiredTimeStamp = -1;

	/**
	 * Runnable which performs timed dispatching of packets from the input stream.
	 */
	private Runnable asyncSender = new Runnable() {
		public void run() {
			try {
				while (ctx == null && !terminate) {
					Thread.sleep(10);
				}
				REPLAY_LOOP: while (!terminate) {
					synchronized (FullReplaySender.this) {
						if (replayIn == null) {
							replayIn = replayFile.getPacketData(getPacketTypeRegistry(true));
						}
						// Packet loop
						while (true) {
							try {
								// When playback is paused and the world has loaded (we don't want any
								// dirt-screens) we sleep
								while (paused() && hasWorldLoaded) {
									// Unless we are going to terminate, restart or jump
									if (terminate || startFromBeginning || desiredTimeStamp != -1) {
										break;
									}
									Thread.sleep(10);
								}

								if (terminate) {
									break REPLAY_LOOP;
								}

								if (startFromBeginning) {
									// In case we need to restart from the beginning
									// break out of the loop sending all packets which will
									// cause the replay to be restarted by the outer loop
									break;
								}

								// Read the next packet if we don't already have one
								if (nextPacket == null) {
									nextPacket = new PacketData(replayIn, loginPhase);
								}

								int nextTimeStamp = nextPacket.timestamp;

								// If we aren't jumping and the world has already been loaded (no dirt-screens)
								// then wait
								// the required amount to get proper packet timing
								if (!isHurrying() && hasWorldLoaded) {
									// Timestamp of when the next packet should be sent
									long expectedTime = realTimeStart + (long) (nextTimeStamp / replaySpeed);
									long now = System.currentTimeMillis();
									// If the packet should not yet be sent, wait a bit
									if (expectedTime > now) {
										Thread.sleep(expectedTime - now);
									}
								}

								// Process packet
								channelRead(ctx, nextPacket.bytes);
								nextPacket = null;

								lastTimeStamp = nextTimeStamp;

								// In case we finished jumping
								// We need to check that we aren't planing to restart so we don't accidentally
								// run this
								// code before we actually restarted
								if (isHurrying() && lastTimeStamp > desiredTimeStamp && !startFromBeginning) {
									desiredTimeStamp = -1;

									replayHandler.moveCameraToTargetPosition();

									// Pause after jumping (this will also reset realTimeStart accordingly)
									setReplaySpeed(0);
								}
							} catch (EOFException eof) {
								// Reached end of file
								// Pause the replay which will cause it to freeze before getting restarted
								setReplaySpeed(0);
								// Then wait until the user tells us to continue
								while (paused() && hasWorldLoaded && desiredTimeStamp == -1 && !terminate) {
									Thread.sleep(10);
								}

								if (terminate) {
									break REPLAY_LOOP;
								}
								break;
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						// Restart the replay.
						hasWorldLoaded = false;
						lastTimeStamp = 0;
						loginPhase = true;
						startFromBeginning = false;
						nextPacket = null;
						realTimeStart = System.currentTimeMillis();
						if (replayIn != null) {
							replayIn.close();
							replayIn = null;
						}
						ReplayMod.instance.runSync(replayHandler::restartedReplay);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Return whether this replay sender is currently rushing. When rushing, all
	 * packets are sent without waiting until a specified timestamp is passed.
	 *
	 * @return {@code true} if currently rushing, {@code false} otherwise
	 */
	public boolean isHurrying() {
		return desiredTimeStamp != -1;
	}

	/**
	 * Cancels the hurrying.
	 */
	public void stopHurrying() {
		desiredTimeStamp = -1;
	}

	/**
	 * Return the timestamp to which this replay sender is currently rushing. All
	 * packets with an lower or equal timestamp will be sent out without any
	 * sleeping.
	 *
	 * @return The timestamp in milliseconds since the start of the replay
	 */
	public long getDesiredTimestamp() {
		return desiredTimeStamp;
	}

	/**
	 * Jumps to the specified timestamp when in async mode by rushing all packets
	 * until one with a timestamp greater than the specified timestamp is found. If
	 * the timestamp has already passed, this causes the replay to restart and then
	 * rush all packets.
	 *
	 * @param millis Timestamp in milliseconds since the start of the replay
	 */
	@Override
	public void jumpToTime(int millis) {
		Preconditions.checkState(asyncMode, "Can only jump in async mode. Use sendPacketsTill(int) instead.");
		if (millis < lastTimeStamp && !isHurrying()) {
			startFromBeginning = true;
		}

		desiredTimeStamp = millis;
	}

	protected Packet processPacketAsync(Packet p) {
		// If hurrying, ignore some packets, except for short durations
		if (desiredTimeStamp - lastTimeStamp > 1000) {
			if (p instanceof ClientboundLevelParticlesPacket)
				return null;

			if (p instanceof ClientboundAddEntityPacket) {
				ClientboundAddEntityPacket pso = (ClientboundAddEntityPacket) p;
				if (pso.getType() == EntityType.FIREWORK_ROCKET)
					return null;
			}
		}
		return p;
	}

	/////////////////////////////////////////////////////////
	// Synchronous packet processing //
	/////////////////////////////////////////////////////////

	/**
	 * Sends all packets until the specified timestamp is reached (inclusive). If
	 * the timestamp is smaller than the last packet sent, the replay is restarted
	 * from the beginning.
	 *
	 * @param timestamp The timestamp in milliseconds since the beginning of this
	 *                  replay
	 */
	private final ExecutorService syncSender = Executors
			.newSingleThreadExecutor(runnable -> new Thread(runnable, "replaymod-sync-sender"));

	@Override
	public void sendPacketsTill(int timestamp) {
		Preconditions.checkState(!asyncMode, "This method cannot be used in async mode. Use jumpToTime(int) instead.");

		// Submit our target to the sender thread and track its progress
		AtomicBoolean doneSending = new AtomicBoolean();
		syncSender.submit(() -> {
			try {
				doSendPacketsTill(timestamp);
			} finally {
				doneSending.set(true);
			}
		});

		// Drain the task queue while we are sending (in case a mod blocks the io thread
		// waiting for the main thread)
		while (!doneSending.get()) {
			executeTaskQueue();

			// Wait until the sender thread has made progress
			try {
				// noinspection BusyWait
				Thread.sleep(0, 100_000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}

		// Everything has been sent, drain the queue one last time
		executeTaskQueue();
	}

	private void executeTaskQueue() {
		((MinecraftMethodAccessor) mc).replayModExecuteTaskQueue();
		ReplayMod.instance.runTasks();
	}

	private void doSendPacketsTill(int timestamp) {
		try {
			while (ctx == null && !terminate) { // Make sure channel is ready
				Thread.sleep(10);
			}

			synchronized (this) {
				if (timestamp == lastTimeStamp) { // Do nothing if we're already there
					return;
				}
				if (timestamp < lastTimeStamp) { // Restart the replay if we need to go backwards in time
					hasWorldLoaded = false;
					lastTimeStamp = 0;
					if (replayIn != null) {
						replayIn.close();
						replayIn = null;
					}
					loginPhase = true;
					startFromBeginning = false;
					nextPacket = null;
					ReplayMod.instance.runSync(replayHandler::restartedReplay);
				}

				if (replayIn == null) {
					replayIn = replayFile.getPacketData(getPacketTypeRegistry(true));
				}

				while (true) { // Send packets
					try {
						PacketData pd;
						if (nextPacket != null) {
							// If there is still a packet left from before, use it first
							pd = nextPacket;
							nextPacket = null;
						} else {
							// Otherwise read one from the input stream
							pd = new PacketData(replayIn, loginPhase);
						}

						int nextTimeStamp = pd.timestamp;
						if (nextTimeStamp > timestamp) {
							// We are done sending all packets
							nextPacket = pd;
							break;
						}

						// Process packet
						channelRead(ctx, pd.bytes);
					} catch (EOFException eof) {
						// Shit! We hit the end before finishing our job! What shall we do now?
						// well, let's just pretend we're done...
						replayIn = null;
						break;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				// This might be required if we change to async mode anytime soon
				realTimeStart = System.currentTimeMillis() - (long) (timestamp / replaySpeed);
				lastTimeStamp = timestamp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Packet processPacketSync(Packet p) {
		if (p instanceof ClientboundForgetLevelChunkPacket) {
			ClientboundForgetLevelChunkPacket packet = (ClientboundForgetLevelChunkPacket) p;
			int x = packet.getX();
			int z = packet.getZ();
			// If the chunk is getting unloaded, we will have to forcefully update the
			// position of all entities
			// within. Otherwise, if there wasn't a game tick recently, there may be
			// entities that have moved
			// out of the chunk by now but are still registered in it. If we do not update
			// those, they will get
			// unloaded even though they shouldn't.
			// Note: This is only half of the truth. Entities may be removed by
			// chunk-unloading, see else-case below.
			// To make things worse, it seems like players were never supposed to be
			// unloaded this way because
			// they will remain glitched in the World#playerEntities list.
			// 1.14+: The update issue remains but only for non-players and the unloading
			// list bug appears to have been
			// fixed (chunk unloading no longer removes the entities).
			// Get the chunk that will be unloaded
			ClientLevel world = mc.level;
			ClientChunkCache chunkProvider = world.getChunkSource();
			LevelChunk chunk = chunkProvider.getChunkNow(x, z);
			if (chunk != null) {
				List<Entity> entitiesInChunk = new ArrayList<>();
				// #else
				for (Entity entity : mc.level.entitiesForRendering()) {
					if (entity.chunkPosition().equals(chunk.getPos())) {
						entitiesInChunk.add(entity);
					}
				}
				// #endif
				for (Entity entity : entitiesInChunk) {
					// Skip interpolation of position updates coming from server
					// (See: newX in EntityLivingBase or otherPlayerMPX in EntityOtherPlayerMP)
					forcePositionForVehicleAndSelf(entity);
				}
			}
		}
		return p; // During synchronous playback everything is sent normally
	}

	private void forcePositionForVehicleAndSelf(Entity entity) {
		Entity vehicle = entity.getVehicle();
		if (vehicle != null) {
			forcePositionForVehicleAndSelf(vehicle);
		}

		// Skip interpolation of position updates coming from server
		// (See: newX in EntityLivingBase or otherPlayerMPX in EntityOtherPlayerMP)
		int ticks = 0;
		Vec3 prevPos;
		do {
			prevPos = entity.position();
			if (vehicle != null) {
				entity.rideTick();
				;
			} else {
				entity.tick();
			}
		} while (prevPos.distanceToSqr(entity.position()) > 0.0001 && ticks++ < 100);
	}

	private static final class PacketData {
		private static final com.github.steveice10.netty.buffer.ByteBuf byteBuf = com.github.steveice10.netty.buffer.Unpooled
				.buffer();
		private static final NetOutput netOutput = new ByteBufNetOutput(byteBuf);

		private final int timestamp;
		private final byte[] bytes;

		PacketData(ReplayInputStream in, boolean loginPhase) throws IOException {
			if (ReplayMod.isMinimalMode()) {
				// Minimal mode, we can only read our exact protocol version and cannot use
				// ReplayStudio
				timestamp = readInt(in);
				int length = readInt(in);
				if (timestamp == -1 || length == -1) {
					throw new EOFException();
				}
				bytes = new byte[length];
				IOUtils.readFully(in, bytes);
			} else {
				com.replaymod.replaystudio.PacketData data = in.readPacket();
				if (data == null) {
					throw new EOFException();
				}
				timestamp = (int) data.getTime();
				com.replaymod.replaystudio.protocol.Packet packet = data.getPacket();
				// We need to re-encode ReplayStudio packets, so we can later decode them as NMS
				// packets
				// The main reason we aren't reading them as NMS packets is that we want
				// ReplayStudio to be able
				// to apply ViaVersion (and potentially other magic) to it.
				synchronized (byteBuf) {
					byteBuf.markReaderIndex(); // Mark the current reader and writer index (should be at start)
					byteBuf.markWriterIndex();

					netOutput.writeVarInt(packet.getId());
					int idSize = byteBuf.readableBytes();
					int contentSize = packet.getBuf().readableBytes();
					bytes = new byte[idSize + contentSize]; // Create bytes array of sufficient size
					byteBuf.readBytes(bytes, 0, idSize);
					packet.getBuf().readBytes(bytes, idSize, contentSize);

					byteBuf.resetReaderIndex(); // Reset reader & writer index for next use
					byteBuf.resetWriterIndex();
				}
				packet.getBuf().release();
			}
		}
	}
}
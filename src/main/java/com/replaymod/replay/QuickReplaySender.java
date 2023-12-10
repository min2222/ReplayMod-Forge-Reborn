package com.replaymod.replay;

import static com.replaymod.core.versions.MCVer.getMinecraft;
import static com.replaymod.core.versions.MCVer.getPacketTypeRegistry;
import static com.replaymod.replay.ReplayModReplay.LOGGER;

import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.steveice10.packetlib.io.NetInput;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.replay.mixin.MinecraftAccessor;
import com.replaymod.replay.mixin.TimerAccessor;
import com.replaymod.replaystudio.rar.RandomAccessReplay;
import com.replaymod.replaystudio.replay.ReplayFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

/**
 * Sends only chunk updates and entity position updates but tries to do so as
 * quickly as possible. To do so, it performs an initial analysis of the replay,
 * scanning all of its packets and storing entity positions and chunk states
 * while doing so. This allows it to later jump to any time by doing a diff from
 * the current time (including backwards jumping).
 */
@ChannelHandler.Sharable
public class QuickReplaySender extends ChannelHandlerAdapter implements ReplaySender {
	private final Minecraft mc = getMinecraft();

	private final ReplayModReplay mod;
	private final RandomAccessReplay replay;
	private final EventHandler eventHandler = new EventHandler();
	private ChannelHandlerContext ctx;

	private int currentTimeStamp;
	private double replaySpeed = 1;

	/**
	 * Whether async mode is enabled. Async mode is emulated by registering an event
	 * handler on client tick.
	 */
	private boolean asyncMode;
	private long lastAsyncUpdateTime;

	private ListenableFuture<Void> initPromise;

	private com.github.steveice10.netty.buffer.ByteBuf buf;
	private NetInput bufInput;

	public QuickReplaySender(ReplayModReplay mod, ReplayFile replayFile) {
		this.mod = mod;
		this.replay = new RandomAccessReplay(replayFile, getPacketTypeRegistry(false)) {
			private byte[] buf = new byte[0];

			@Override
			protected void dispatch(com.replaymod.replaystudio.protocol.Packet packet) {
				com.github.steveice10.netty.buffer.ByteBuf byteBuf = packet.getBuf();
				int size = byteBuf.readableBytes();
				if (buf.length < size) {
					buf = new byte[size];
				}
				byteBuf.getBytes(byteBuf.readerIndex(), buf, 0, size);
				ByteBuf wrappedBuf = Unpooled.wrappedBuffer(buf);
				wrappedBuf.writerIndex(size);
				FriendlyByteBuf packetByteBuf = new FriendlyByteBuf(wrappedBuf);

				Packet<?> mcPacket;
				mcPacket = ConnectionProtocol.PLAY.createPacket(PacketFlow.CLIENTBOUND, packet.getId(), packetByteBuf);
				if (mcPacket != null) {
					mcPacket.write(packetByteBuf);
					ctx.fireChannelRead(mcPacket);
				}
			}
		};
	}

	public void register() {
		eventHandler.register();
	}

	public void unregister() {
		eventHandler.unregister();
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public ListenableFuture<Void> getInitializationPromise() {
		return initPromise;
	}

	public ListenableFuture<Void> initialize(Consumer<Double> progress) {
		if (initPromise != null) {
			return initPromise;
		}
		SettableFuture<Void> promise = SettableFuture.create();
		initPromise = promise;
		new Thread(() -> {
			try {
				long start = System.currentTimeMillis();
				replay.load(progress);
				LOGGER.info("Initialized quick replay sender in " + (System.currentTimeMillis() - start) + "ms");
			} catch (Throwable e) {
				LOGGER.error("Initializing quick replay sender:", e);
				mod.getCore().runLater(() -> {
					mod.getCore().printWarningToChat("Error initializing quick replay sender: %s",
							e.getLocalizedMessage());
					promise.setException(e);
				});
				return;
			}
			mod.getCore().runLater(() -> promise.set(null));
		}).start();
		return promise;
	}

	private void ensureInitialized(Runnable body) {
		if (initPromise == null) {
			LOGGER.warn("QuickReplaySender used without prior initialization!", new Throwable());
			initialize(progress -> {
			});
		}
		Futures.addCallback(initPromise, new FutureCallback<Void>() {
			@Override
			public void onSuccess(@Nullable Void result) {
				body.run();
			}

			@Override
			public void onFailure(Throwable t) {
				// Error already printed by initialize method
			}
		}, Runnable::run);
	}

	public void restart() {
		replay.reset();
	}

	@Override
	public int currentTimeStamp() {
		return currentTimeStamp;
	}

	@Override
	public void setReplaySpeed(double factor) {
		if (factor != 0) {
			if (paused() && asyncMode) {
				lastAsyncUpdateTime = System.currentTimeMillis();
			}
			this.replaySpeed = factor;
		}
		TimerAccessor timer = (TimerAccessor) ((MinecraftAccessor) mc).getTimer();
		timer.setTickLength(WrappedTimer.DEFAULT_MS_PER_TICK / (float) factor);
	}

	@Override
	public double getReplaySpeed() {
		return replaySpeed;
	}

	@Override
	public boolean isAsyncMode() {
		return asyncMode;
	}

	@Override
	public void setAsyncMode(boolean async) {
		if (this.asyncMode == async)
			return;
		ensureInitialized(() -> {
			this.asyncMode = async;
			if (async) {
				lastAsyncUpdateTime = System.currentTimeMillis();
			}
		});
	}

	@Override
	public void setSyncModeAndWait() {
		setAsyncMode(false);
		// No waiting required, we emulated async mode via tick events
	}

	@Override
	public void jumpToTime(int value) {
		sendPacketsTill(value);
	}

	private class EventHandler extends EventRegistrations {
		{
			on(PreTickCallback.EVENT, this::onTick);
		}

		private void onTick() {
			if (!asyncMode || paused())
				return;

			long now = System.currentTimeMillis();
			long realTimePassed = now - lastAsyncUpdateTime;
			lastAsyncUpdateTime = now;
			int replayTimePassed = (int) (realTimePassed * replaySpeed);
			sendPacketsTill(currentTimeStamp + replayTimePassed);
		}
	}

	@Override
	public void sendPacketsTill(int replayTime) {
		ensureInitialized(() -> {
			try {
				replay.seek(replayTime);
			} catch (IOException e) {
				e.printStackTrace();
			}
			currentTimeStamp = replayTime;
		});
	}
}
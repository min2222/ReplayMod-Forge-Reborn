package com.replaymod.replay.mixin;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.versions.MCVer;
import com.replaymod.recording.handler.RecordingEventHandler;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.packets.PacketPlayerListEntry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;

@Mixin({ ClientPacketListener.class })
public abstract class MixinNetHandlerPlayClient {
	private static Minecraft mcStatic = MCVer.getMinecraft();
	@Shadow
	private Map<UUID, PlayerInfo> playerInfoMap;

	public RecordingEventHandler getRecordingEventHandler() {
		return ((RecordingEventHandler.RecordingEventSender) mcStatic.levelRenderer).getRecordingEventHandler();
	}

	@Inject(method = { "handlePlayerInfo" }, at = { @At("HEAD") })
	public void recordOwnJoin(ClientboundPlayerInfoPacket packet, CallbackInfo ci) {
		if (mcStatic.isSameThread()) {
			if (mcStatic.player != null) {
				RecordingEventHandler handler = this.getRecordingEventHandler();
				if (handler != null && packet.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
					ByteBuf byteBuf = Unpooled.buffer();

					try {
						packet.write(new FriendlyByteBuf(byteBuf));
						byteBuf.readerIndex(0);
						byte[] array = new byte[byteBuf.readableBytes()];
						byteBuf.readBytes(array);
						Iterator var6 = PacketPlayerListEntry
								.read(new Packet(MCVer.getPacketTypeRegistry(false), 0, PacketType.PlayerListEntry,
										com.github.steveice10.netty.buffer.Unpooled.wrappedBuffer(array)))
								.iterator();

						while (var6.hasNext()) {
							PacketPlayerListEntry data = (PacketPlayerListEntry) var6.next();
							if (data.getUuid() != null
									&& data.getUuid().equals(mcStatic.player.getGameProfile().getId())
									&& !this.playerInfoMap.containsKey(data.getUuid())) {
								handler.spawnRecordingPlayer();
							}
						}
					} catch (IOException var11) {
						throw new RuntimeException(var11);
					} finally {
						byteBuf.release();
					}
				}

			}
		}
	}

	@Inject(method = { "handleRespawn" }, at = { @At("RETURN") })
	public void recordOwnRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
		RecordingEventHandler handler = this.getRecordingEventHandler();
		if (handler != null) {
			handler.spawnRecordingPlayer();
		}

	}
}

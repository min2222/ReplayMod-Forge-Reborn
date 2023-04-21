package com.replaymod.mixin;

import java.io.IOException;
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

@Mixin(ClientPacketListener.class)
public abstract class MixinNetHandlerPlayClient {

    // The stupid name is required as otherwise Mixin treats it as a shadow, seemingly ignoring the lack of @Shadow
    private static Minecraft mcStatic = MCVer.getMinecraft();

    @Shadow
    private Map<UUID, PlayerInfo> playerInfoMap;
	
    public RecordingEventHandler getRecordingEventHandler() {
        return ((RecordingEventHandler.RecordingEventSender) mcStatic.levelRenderer).getRecordingEventHandler();
    }

    /**
     * Record the own player entity joining the world.
     * We cannot use the {@link net.minecraftforge.event.entity.EntityJoinWorldEvent} because the entity id
     * of the player is set afterwards and the tablist entry might not yet be sent.
     *
     * @param packet The packet
     * @param ci     Callback info
     */
    @Inject(method = "handlePlayerInfo", at = @At("HEAD"))
    public void recordOwnJoin(ClientboundPlayerInfoPacket packet, CallbackInfo ci) {
        if (!mcStatic.isSameThread()) return;
        if (mcStatic.player == null) return;

        RecordingEventHandler handler = getRecordingEventHandler();
        if (handler != null && packet.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
            ByteBuf byteBuf = Unpooled.buffer();
            try {
                packet.write(new FriendlyByteBuf(byteBuf));

                byteBuf.readerIndex(0);
                byte[] array = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(array);

                for (PacketPlayerListEntry data : PacketPlayerListEntry.read(new Packet(
                        MCVer.getPacketTypeRegistry(false), 0, PacketType.PlayerListEntry,
                        com.github.steveice10.netty.buffer.Unpooled.wrappedBuffer(array)
                ))) {
                    if (data.getUuid() == null) continue;
                    // Only add spawn packet for our own player and only if he isn't known yet
                    if (data.getUuid().equals(mcStatic.player.getGameProfile().getId())
                            && !this.playerInfoMap.containsKey(data.getUuid())) {
                        handler.spawnRecordingPlayer();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e); // we just parsed this?
            } finally {
                byteBuf.release();
            }
        }
    }

    /**
     * Record the own player entity respawning.
     * We cannot use the {@link net.minecraftforge.event.entity.EntityJoinWorldEvent} because that would also include
     * the first spawn which is already handled by the above method.
     *
     * @param packet The packet
     * @param ci     Callback info
     */
    @Inject(method = "handleRespawn", at = @At("RETURN"))
    public void recordOwnRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        RecordingEventHandler handler = getRecordingEventHandler();
        if (handler != null) {
            handler.spawnRecordingPlayer();
        }
    }
}

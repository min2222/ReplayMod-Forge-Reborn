package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.versions.MCVer;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.recording.handler.RecordingEventHandler;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;

@Mixin(ClientHandshakePacketListenerImpl.class)
public abstract class MixinNetHandlerLoginClient {

    @Shadow
    private @Final Connection connection;

    @Inject(method = "handleGameProfile", at = @At("HEAD"))
    public void replayModRecording_initiateRecording(ClientboundGameProfilePacket p_104545_, CallbackInfo cb) {
        initiateRecording(p_104545_);
    }

    /**
     * Starts the recording right before switching into PLAY state.
     * We cannot use the {@link FMLNetworkEvent.ClientConnectedToServerEvent}
     * as it only fires after the forge handshake.
     */
    @Inject(method = "handleCustomQuery", at = @At("HEAD"))
    public void replayModRecording_initiateRecording(ClientboundCustomQueryPacket packetIn, CallbackInfo cb) {
        initiateRecording(packetIn);
    }

    private void initiateRecording(Packet<?> packet) {
        RecordingEventHandler.RecordingEventSender eventSender = (RecordingEventHandler.RecordingEventSender) MCVer.getMinecraft().levelRenderer;
        if (eventSender.getRecordingEventHandler() != null) {
            return; // already recording
        }
        ReplayModRecording.instance.initiateRecording(this.connection);
        if (eventSender.getRecordingEventHandler() != null && packet != null) {
            eventSender.getRecordingEventHandler().onPacket(packet);
        }
    }
}

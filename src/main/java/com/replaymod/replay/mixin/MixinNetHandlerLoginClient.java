package com.replaymod.replay.mixin;

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

@Mixin({ ClientHandshakePacketListenerImpl.class })
public abstract class MixinNetHandlerLoginClient {
	@Final
	@Shadow
	private Connection connection;

	@Inject(method = { "handleGameProfile" }, at = { @At("HEAD") })
	private void earlyInitiateRecording(ClientboundGameProfilePacket packet, CallbackInfo ci) {
		this.initiateRecording(packet);
	}

	@Inject(method = { "handleCustomQuery" }, at = { @At("HEAD") })
	private void lateInitiateRecording(ClientboundCustomQueryPacket packet, CallbackInfo ci) {
		this.initiateRecording(packet);
	}

	private void initiateRecording(Packet<?> packet) {
		RecordingEventHandler.RecordingEventSender eventSender = (RecordingEventHandler.RecordingEventSender) MCVer
				.getMinecraft().levelRenderer;
		if (eventSender.getRecordingEventHandler() == null) {
			ReplayModRecording.instance.initiateRecording(this.connection);
			if (eventSender.getRecordingEventHandler() != null) {
				eventSender.getRecordingEventHandler().onPacket(packet);
			}

		}
	}
}

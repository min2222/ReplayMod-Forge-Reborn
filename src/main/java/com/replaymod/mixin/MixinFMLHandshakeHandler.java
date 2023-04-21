package com.replaymod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.HandshakeHandler;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;

@Mixin(HandshakeHandler.class)
public abstract class MixinFMLHandshakeHandler {
    @Shadow
    private List<NetworkRegistry.LoginPayload> messageList;

    @Shadow
    @Final
    private NetworkDirection direction;

    @Inject(method = "<init>(Lnet/minecraft/network/Connection;Lnet/minecraftforge/network/NetworkDirection;)V", at = @At("TAIL"))
    public void replayModRecording_setupForLocalRecording(Connection networkManager, NetworkDirection side, CallbackInfo ci) {
        if (!networkManager.isMemoryConnection()) {
            return;
        }
        System.out.println("Force FML handshaking and set LoginPayloads");
        this.messageList = NetworkRegistryAccessor.invokeGatherLoginPayloads(this.direction, false);
    }

    @Redirect(method = "handleRegistryLoading", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    public void replayMod_ignoreHandshakeConnectionClose(Connection networkManager, Component message) {
    }
}

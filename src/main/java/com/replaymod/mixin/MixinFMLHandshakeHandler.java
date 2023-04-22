package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraftforge.network.HandshakeHandler;

@Mixin(HandshakeHandler.class)
public abstract class MixinFMLHandshakeHandler {
	//TODO unable to join old worlds
    /*@Shadow
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
    }*/
}

package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetHandler {
	//TODO
	/*@Shadow
    @Final
    private Connection connection;

    @Shadow
    private Minecraft client;

    @Shadow
    private ITagCollectionSupplier networkTagManager;

    @Inject(method = "handleUpdateTags", at = @At(value = "HEAD"), cancellable = true)
    public void replayMod_ignoreHandshakeConnectionClose(ClientboundUpdateTagsPacket packetIn, CallbackInfo ci) {
        System.out.println("Injected ClientPlayNetHandler.handleTags");
        // PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.client);
        ITagCollectionSupplier itagcollectionsupplier = packetIn.getTags();
        // boolean vanillaConnection = net.minecraftforge.fml.network.NetworkHooks.isVanillaConnection(netManager);
        boolean vanillaConnection = false;
        net.minecraftforge.common.ForgeTagHandler.resetCachedTagCollections(true, vanillaConnection);
        itagcollectionsupplier = ITagCollectionSupplier.reinjectOptionalTags(itagcollectionsupplier);
        this.networkTagManager = itagcollectionsupplier;
        if (!this.connection.isMemoryConnection()) {
            itagcollectionsupplier.updateTags();
        }

        this.client.getSearchTree(SearchTreeManager.TAGS).recalculate();

        ci.cancel();
    }*/
}

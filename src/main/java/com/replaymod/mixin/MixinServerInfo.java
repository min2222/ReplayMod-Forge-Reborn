package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.replaymod.recording.ServerInfoExt;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;

@Mixin(ServerData.class)
public abstract class MixinServerInfo implements ServerInfoExt {
    private Boolean autoRecording;

    @Override
    public Boolean getAutoRecording() {
        return autoRecording;
    }

    @Override
    public void setAutoRecording(Boolean autoRecording) {
        this.autoRecording = autoRecording;
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void serialize(CallbackInfoReturnable<CompoundTag> ci) {
        CompoundTag tag = ci.getReturnValue();
        if (autoRecording != null) {
            tag.putBoolean("autoRecording", autoRecording);
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void deserialize(CompoundTag tag, CallbackInfoReturnable<ServerData> ci) {
        ServerInfoExt serverInfo = ServerInfoExt.from(ci.getReturnValue());
        if (tag.contains("autoRecording")) {
            serverInfo.setAutoRecording(tag.getBoolean("autoRecording"));
        }
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    public void copyFrom(ServerData serverInfo, CallbackInfo ci) {
        ServerInfoExt from = ServerInfoExt.from(serverInfo);
        this.autoRecording = from.getAutoRecording();
    }
}

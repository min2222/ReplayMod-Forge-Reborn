package com.replaymod.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.recording.handler.RecordingEventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;


@Mixin(ClientLevel.class)
public abstract class MixinWorldClient extends Level implements RecordingEventHandler.RecordingEventSender {
    @Shadow
    private Minecraft minecraft;

    protected MixinWorldClient(WritableLevelData mutableWorldProperties, ResourceKey<Level> registryKey, Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean bl, boolean bl2, long l, int p_220359_) {
        super(mutableWorldProperties, registryKey,
                dimensionType, profiler, bl, bl2, l, p_220359_);
    }

    private RecordingEventHandler replayModRecording_getRecordingEventHandler() {
        return ((RecordingEventHandler.RecordingEventSender) this.minecraft.levelRenderer).getRecordingEventHandler();
    }

    // Sounds that are emitted by thePlayer no longer take the long way over the server
    // but are instead played directly by the client. The server only sends these sounds to
    // other clients so we have to record them manually.
    // E.g. Block place sounds
    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"))
    public void replayModRecording_recordClientSound(Player player, double x, double y, double z, SoundEvent sound, SoundSource category,
                                                     float volume, float pitch, long p_233629_, CallbackInfo ci) {
        if (player == this.minecraft.player) {
            RecordingEventHandler handler = replayModRecording_getRecordingEventHandler();
            if (handler != null) {
                handler.onClientSound(sound, category, x, y, z, volume, pitch);
            }
        }
    }

    //TODO
    // Same goes for level events (also called effects). E.g. door open, block break, etc.
    @Inject(method = "syncWorldEvent", at = @At("HEAD"))
    private void playLevelEvent(Player player, int type, BlockPos pos, int data, CallbackInfo ci) {
        if (player == this.minecraft.player) {
            // We caused this event, the server won't send it to us
            RecordingEventHandler handler = replayModRecording_getRecordingEventHandler();
            if (handler != null) {
                handler.onClientEffect(type, pos, data);
            }
        }
    }
}

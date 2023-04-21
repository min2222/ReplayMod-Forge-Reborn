package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.recording.handler.RecordingEventHandler;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;

@Mixin(LevelRenderer.class)
public abstract class MixinRenderGlobal implements RecordingEventHandler.RecordingEventSender {

    private RecordingEventHandler recordingEventHandler;

    @Override
    public void setRecordingEventHandler(RecordingEventHandler recordingEventHandler) {
        this.recordingEventHandler = recordingEventHandler;
    }

    @Override
    public RecordingEventHandler getRecordingEventHandler() {
        return recordingEventHandler;
    }

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    public void saveBlockBreakProgressPacket(int breakerId,
                                             BlockPos pos,
                                             int progress, CallbackInfo info) {
        if (recordingEventHandler != null) {
            recordingEventHandler.onBlockBreakAnim(breakerId,
                    pos,
                    progress);
        }
    }
}

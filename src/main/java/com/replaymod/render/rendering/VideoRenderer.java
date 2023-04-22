package com.replaymod.render.rendering;

import static com.google.common.collect.Iterables.getLast;
import static com.replaymod.core.versions.MCVer.resizeMainWindow;
import static com.replaymod.render.ReplayModRender.LOGGER;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Window;
//#if MC>=11500
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.replaymod.core.MinecraftMethodAccessor;
import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.core.versions.MCVer;
import com.replaymod.mixin.MinecraftAccessor;
import com.replaymod.mixin.TimerAccessor;
import com.replaymod.pathing.player.AbstractTimelinePlayer;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.render.CameraPathExporter;
import com.replaymod.render.EXRWriter;
import com.replaymod.render.FFmpegWriter;
import com.replaymod.render.PNGWriter;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.ReplayModRender;
import com.replaymod.render.blend.BlendState;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.events.ReplayRenderCallback;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.gui.GuiRenderingDone;
import com.replaymod.render.gui.GuiVideoRenderer;
import com.replaymod.render.gui.progress.VirtualWindow;
import com.replaymod.render.hooks.ForceChunkLoadingHook;
import com.replaymod.render.metadata.MetadataInjector;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;

import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
//#endif
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class VideoRenderer implements RenderInfo {
    private static final ResourceLocation SOUND_RENDER_SUCCESS = new ResourceLocation("replaymod", "render_success");
    private final Minecraft mc = MCVer.getMinecraft();
    private final RenderSettings settings;
    private final ReplayHandler replayHandler;
    private final Timeline timeline;
    private final Pipeline renderingPipeline;
    private final FFmpegWriter ffmpegWriter;
    private final CameraPathExporter cameraPathExporter;

    private int fps;
    private boolean mouseWasGrabbed;
    private boolean debugInfoWasShown;
    private Map<SoundSource, Float> originalSoundLevels;

    private TimelinePlayer timelinePlayer;
    private Future<Void> timelinePlayerFuture;
    private ForceChunkLoadingHook forceChunkLoadingHook;
    private int framesDone;
    private int totalFrames;

    private final VirtualWindow guiWindow = new VirtualWindow(mc);
    private final GuiVideoRenderer gui;
    private boolean paused;
    private boolean cancelled;
    private volatile Throwable failureCause;

    public VideoRenderer(RenderSettings settings, ReplayHandler replayHandler, Timeline timeline) throws IOException {
        this.settings = settings;
        this.replayHandler = replayHandler;
        this.timeline = timeline;
        this.gui = new GuiVideoRenderer(this);
        if (settings.getRenderMethod() == RenderSettings.RenderMethod.BLEND) {
            BlendState.setState(new BlendState(settings.getOutputFile()));

            this.renderingPipeline = Pipelines.newBlendPipeline(this);
            this.ffmpegWriter = null;
        } else {
            FrameConsumer<BitmapFrame> frameConsumer;
            if (settings.getEncodingPreset() == RenderSettings.EncodingPreset.EXR) {
                frameConsumer = EXRWriter.create(settings.getOutputFile().toPath(), settings.isIncludeAlphaChannel());
            } else if (settings.getEncodingPreset() == RenderSettings.EncodingPreset.PNG) {
                frameConsumer = new PNGWriter(settings.getOutputFile().toPath(), settings.isIncludeAlphaChannel());
            } else {
                frameConsumer = new FFmpegWriter(this);
            }
            ffmpegWriter = frameConsumer instanceof FFmpegWriter ? (FFmpegWriter) frameConsumer : null;
            FrameConsumer<BitmapFrame> previewingFrameConsumer = new FrameConsumer<BitmapFrame>() {
                private int lastFrameId = -1;

                @Override
                public void consume(Map<Channel, BitmapFrame> channels) {
                    BitmapFrame bgra = channels.get(Channel.BRGA);
                    if (bgra != null) {
                        synchronized (this) {
                            int frameId = bgra.getFrameId();
                            if (lastFrameId < frameId) {
                                lastFrameId = frameId;
                                gui.updatePreview(bgra.getByteBuffer(), bgra.getSize());
                            }
                        }
                    }
                    frameConsumer.consume(channels);
                }

                @Override
                public void close() throws IOException {
                    frameConsumer.close();
                }

                @Override
                public boolean isParallelCapable() {
                    return frameConsumer.isParallelCapable();
                }
            };
            this.renderingPipeline = Pipelines.newPipeline(settings.getRenderMethod(), this, previewingFrameConsumer);
        }

        if (settings.isCameraPathExport()) {
            this.cameraPathExporter = new CameraPathExporter(settings);
        } else {
            this.cameraPathExporter = null;
        }
    }

    /**
     * Render this video.
     * @return {@code true} if rendering was successful, {@code false} if the user aborted rendering (or the window was closed)
     */
    public boolean renderVideo() throws Throwable {
        ReplayRenderCallback.Pre.EVENT.invoker().beforeRendering(this);

        setup();

        // Because this might take some time to prepare we'll render the GUI at least once to not confuse the user
        drawGui();

        Timer timer = ((MinecraftAccessor) mc).getTimer();

        // Play up to one second before starting to render
        // This is necessary in order to ensure that all entities have at least two position packets
        // and their first position in the recording is correct.
        // Note that it is impossible to also get the interpolation between their latest position
        // and the one in the recording correct as there's no reliable way to tell when the server ticks
        // or when we should be done with the interpolation of the entity
        Optional<Integer> optionalVideoStartTime = timeline.getValue(TimestampProperty.PROPERTY, 0);
        if (optionalVideoStartTime.isPresent()) {
            int videoStart = optionalVideoStartTime.get();

            if (videoStart > 1000) {
                int replayTime = videoStart - 1000;
                //#if MC>=11200
                timer.partialTick = 0;
                ((TimerAccessor) timer).setTickLength(WrappedTimer.DEFAULT_MS_PER_TICK);
                while (replayTime < videoStart) {
                    replayTime += 50;
                    replayHandler.getReplaySender().sendPacketsTill(replayTime);
                    tick();
                }
            }
        }

        renderingPipeline.run();

        if (((MinecraftAccessor) mc).getCrashReporter() != null) {
            throw new ReportedException(((MinecraftAccessor) mc).getCrashReporter().get());
        }

        if (settings.isInjectSphericalMetadata()) {
            MetadataInjector.injectMetadata(settings.getRenderMethod(), settings.getOutputFile(),
                    settings.getTargetVideoWidth(), settings.getTargetVideoHeight(),
                    settings.getSphericalFovX(), settings.getSphericalFovY());
        }

        finish();

        ReplayRenderCallback.Post.EVENT.invoker().afterRendering(this);

        if (failureCause != null) {
            throw failureCause;
        }

        return !cancelled;
    }

    @Override
    public float updateForNextFrame() {
        // because the jGui lib uses Minecraft's displayWidth and displayHeight values, update these temporarily
        guiWindow.bind();

        if (!settings.isHighPerformance() || framesDone % fps == 0) {
            while (drawGui() && paused) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Updating the timer will cause the timeline player to update the game state
        Timer timer = ((MinecraftAccessor) mc).getTimer();
        //#if MC>=11600
        int elapsedTicks =
        timer.advanceTime(
                MCVer.milliTime()
        );
        executeTaskQueue();

        while (elapsedTicks-- > 0) {
            tick();
        }

        // change Minecraft's display size back
        guiWindow.unbind();

        if (cameraPathExporter != null) {
            cameraPathExporter.recordFrame(timer.partialTick);
        }

        framesDone++;
        return timer.partialTick;
    }

    @Override
    public RenderSettings getRenderSettings() {
        return settings;
    }

    private void setup() {
        timelinePlayer = new TimelinePlayer(replayHandler);
        timelinePlayerFuture = timelinePlayer.start(timeline);

        if (mc.options.renderDebug) {
            debugInfoWasShown = true;
            mc.options.renderDebug = false;
        }
        if (mc.mouseHandler.isMouseGrabbed()) {
            mouseWasGrabbed = true;
        }
        mc.mouseHandler.releaseMouse();
        
        originalSoundLevels = new EnumMap<>(SoundSource.class);
        for (SoundSource category : SoundSource.values()) {
            if (category != SoundSource.MASTER) {
                originalSoundLevels.put(category, mc.options.getSoundSourceVolume(category));
                mc.options.setSoundCategoryVolume(category, 0);
            }
        }

        fps = settings.getFramesPerSecond();

        long duration = 0;
        for (Path path : timeline.getPaths()) {
            if (!path.isActive()) continue;

            // Prepare path interpolations
            path.updateAll();
            // Find end time
            Collection<Keyframe> keyframes = path.getKeyframes();
            if (keyframes.size() > 0) {
                duration = Math.max(duration, getLast(keyframes).getTime());
            }
        }

        totalFrames = (int) (duration*fps/1000);

        if (cameraPathExporter != null) {
            cameraPathExporter.setup(totalFrames);
        }

        gui.toMinecraft().init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());

        forceChunkLoadingHook = new ForceChunkLoadingHook(mc.levelRenderer);
    }

    private void finish() {
        if (!timelinePlayerFuture.isDone()) {
            timelinePlayerFuture.cancel(false);
        }
        // Tear down of the timeline player might only happen the next tick after it was cancelled
        timelinePlayer.onTick();

        guiWindow.close();
        mc.options.renderDebug = debugInfoWasShown;
        if (mouseWasGrabbed) {
            mc.mouseHandler.grabMouse();
        }
        for (Map.Entry<SoundSource, Float> entry : originalSoundLevels.entrySet()) {
            mc.options.setSoundCategoryVolume(entry.getKey(), entry.getValue());
        }
        mc.setScreen(null);
        forceChunkLoadingHook.uninstall();

        if (!hasFailed() && cameraPathExporter != null) {
            try {
                cameraPathExporter.finish();
            } catch (IOException e) {
                setFailure(e);
            }
        }

        mc.getSoundManager().play(SimpleSoundInstance.forUI(new SoundEvent(SOUND_RENDER_SUCCESS), 1));

        try {
            if (!hasFailed() && ffmpegWriter != null) {
                new GuiRenderingDone(ReplayModRender.instance, ffmpegWriter.getVideoFile(), totalFrames, settings).display();
            }
        } catch (FFmpegWriter.FFmpegStartupException e) {
            setFailure(e);
        }

        // Finally, resize the Minecraft framebuffer to the actual width/height of the window
        resizeMainWindow(mc, guiWindow.getFramebufferWidth(), guiWindow.getFramebufferHeight());
    }

    private void executeTaskQueue() {
        //#if MC>=11400
        while (true) {
            while (mc.getOverlay() != null) {
                drawGui();
                ((MinecraftMethodAccessor) mc).replayModExecuteTaskQueue();
            }

            CompletableFuture<Void> resourceReloadFuture = ((MinecraftAccessor) mc).getResourceReloadFuture();
            if (resourceReloadFuture != null) {
                ((MinecraftAccessor) mc).setResourceReloadFuture(null);
                mc.reloadResourcePacks().thenRun(() -> resourceReloadFuture.complete(null));
                continue;
            }
            break;
        }
        ((MinecraftMethodAccessor) mc).replayModExecuteTaskQueue();

        mc.screen = gui.toMinecraft();
    }

    private void tick() {
        mc.tick();
    }

    public boolean drawGui() {
        Window window = this.mc.getWindow();
        if (GLFW.glfwWindowShouldClose(window.getWindow()) || ((MinecraftAccessor)this.mc).getCrashReporter() != null)
          return false; 
        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        RenderSystem.clear(16640, false);
        RenderSystem.enableTexture();
        this.guiWindow.beginWrite();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        RenderSystem.setProjectionMatrix(Matrix4f.orthographic(0.0F, 
              
              (float)(window.getWidth() / window.getGuiScale()), 0.0F, 
              
              (float)(window.getHeight() / window.getGuiScale()), 1000.0F, 3000.0F));
        matrixStack.setIdentity();
        matrixStack.translate(0.0D, 0.0D, -2000.0D);
        RenderSystem.applyModelViewMatrix();
        //DiffuseLighting.enableGuiDepthLighting();
        this.gui.toMinecraft().init(this.mc, window.getGuiScaledWidth(), window.getGuiScaledHeight());
        int mouseX = (int)mc.mouseHandler.xpos() * window.getGuiScaledWidth() / Math.max(window.getWidth(), 1);
        int mouseY = (int)mc.mouseHandler.ypos() * window.getGuiScaledHeight() / Math.max(window.getHeight(), 1);
        if (this.mc.getOverlay() != null) {
          Screen orgScreen = this.mc.screen;
          try {
            this.mc.screen = this.gui.toMinecraft();
            this.mc.getOverlay().render(new PoseStack(), mouseX, mouseY, 0.0F);
          } finally {
            this.mc.screen = orgScreen;
          } 
        } else {
          this.gui.toMinecraft().tick();
          this.gui.toMinecraft().render(new PoseStack(), mouseX, mouseY, 0.0F);
        } 
        this.guiWindow.endWrite();
        matrixStack.popPose();
        matrixStack.pushPose();
        this.guiWindow.flip();
        matrixStack.popPose();
        if (mc.mouseHandler.isMouseGrabbed()) {
            mc.mouseHandler.releaseMouse();
        }
        return (!hasFailed() && !this.cancelled);
      }

    public int getFramesDone() {
        return framesDone;
    }

    @Override
    public ReadableDimension getFrameSize() {
        return new Dimension(settings.getVideoWidth(), settings.getVideoHeight());
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public int getVideoTime() { return framesDone * 1000 / fps; }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void cancel() {
        if (ffmpegWriter != null) {
            ffmpegWriter.abort();
        }
        this.cancelled = true;
        renderingPipeline.cancel();
    }

    public boolean hasFailed() {
        return failureCause != null;
    }

    public synchronized void setFailure(Throwable cause) {
        if (this.failureCause != null) {
            LOGGER.error("Further failure during failed rendering: ", cause);
        } else {
            LOGGER.error("Failure during rendering: ", cause);
            this.failureCause = cause;
            cancel();
        }
    }

    private class TimelinePlayer extends AbstractTimelinePlayer {
        public TimelinePlayer(ReplayHandler replayHandler) {
            super(replayHandler);
        }

        @Override
        public long getTimePassed() {
            return getVideoTime();
        }
    }

    public static String[] checkCompat(Stream<RenderSettings> settings) {
        return settings.map(VideoRenderer::checkCompat).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static String[] checkCompat(RenderSettings settings) {
        return null;
    }
}
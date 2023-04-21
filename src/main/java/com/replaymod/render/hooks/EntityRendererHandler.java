package com.replaymod.render.hooks;

import java.io.IOException;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.core.events.PostRenderCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.events.PreRenderHandCallback;
import com.replaymod.core.versions.MCVer;
import com.replaymod.gui.utils.EventRegistrations;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.capturer.CaptureData;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.capturer.WorldRenderer;

import net.minecraft.client.Minecraft;

public class EntityRendererHandler extends EventRegistrations implements WorldRenderer {
    public final Minecraft mc = MCVer.getMinecraft();

    protected final RenderSettings settings;

    private final RenderInfo renderInfo;

    public CaptureData data;

    public boolean omnidirectional;

    public EntityRendererHandler(RenderSettings settings, RenderInfo renderInfo) {
        this.settings = settings;
        this.renderInfo = renderInfo;

        on(PreRenderHandCallback.EVENT, () -> omnidirectional);

        ((IEntityRenderer) mc.gameRenderer).replayModRender_setHandler(this);
        register();
    }

    @Override
    public void renderWorld(final float partialTicks, CaptureData data) {
        this.data = data;
        renderWorld(partialTicks, 0);
    }

    public void renderWorld(float partialTicks, long finishTimeNano) {
        PreRenderCallback.EVENT.invoker().preRender();

        if (mc.level != null && mc.player != null) {
            mc.gameRenderer.renderLevel(partialTicks, finishTimeNano, new PoseStack());
        }

        PostRenderCallback.EVENT.invoker().postRender();
    }

    @Override
    public void close() throws IOException {
        ((IEntityRenderer) mc.gameRenderer).replayModRender_setHandler(null);
        unregister();
    }

    @Override
    public void setOmnidirectional(boolean omnidirectional) {
        this.omnidirectional = omnidirectional;
    }

    public RenderSettings getSettings() {
        return this.settings;
    }

    public RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    public interface IEntityRenderer {
        void replayModRender_setHandler(EntityRendererHandler handler);

        EntityRendererHandler replayModRender_getHandler();
    }
}

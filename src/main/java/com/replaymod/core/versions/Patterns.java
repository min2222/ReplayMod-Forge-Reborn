package com.replaymod.core.versions;

import java.util.Collection;
import java.util.List;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.replaymod.gradle.remap.Pattern;

import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

class Patterns {
    @Pattern
    private static void addCrashCallable(CrashReportCategory category, String name, CrashReportDetail<String> callable) {
        category.setDetail(name, callable);
    }

    @Pattern
    private static double Entity_getX(Entity entity) {
        return entity.getX();
    }

    @Pattern
    private static double Entity_getY(Entity entity) {
        return entity.getY();
    }

    @Pattern
    private static double Entity_getZ(Entity entity) {
        return entity.getZ();
    }

    @Pattern
    private static void Entity_setPos(Entity entity, double x, double y, double z) {
        entity.setPosRaw(x, y, z);
    }

    @Pattern
    private static void setWidth(AbstractWidget button, int value) {
        button.setWidth(value);
    }

    @Pattern
    private static int getWidth(AbstractWidget button) {
        return button.getWidth();
    }

    @Pattern
    private static int getHeight(AbstractWidget button) {
        return button.getHeight();
    }

    @Pattern
    private static String readString(FriendlyByteBuf buffer, int max) {
        return buffer.readUtf(max);
    }

    @Pattern
    private static Entity getRenderViewEntity(Minecraft mc) {
        return mc.getCameraEntity();
    }

    @Pattern
    private static void setRenderViewEntity(Minecraft mc, Entity entity) {
        mc.setCameraEntity(entity);
    }

    @Pattern
    private static Entity getVehicle(Entity passenger) {
        return passenger.getVehicle();
    }

    @Pattern
    private static Iterable<Entity> loadedEntityList(ClientLevel world) {
        return world.entitiesForRendering();
    }

    //TODO not used
    @Pattern
    private static Collection<Entity>[] getEntitySectionArray(LevelChunk chunk) {
        return null;
    }

    @Pattern
    private static List<? extends Player> playerEntities(Level world) {
        return world.players();
    }

    @Pattern
    private static boolean isOnMainThread(Minecraft mc) {
        return mc.isSameThread();
    }

    @Pattern
    private static void scheduleOnMainThread(Minecraft mc, Runnable runnable) {
        mc.tell(runnable);
    }

    @Pattern
    private static Window getWindow(Minecraft mc) {
        return mc.getWindow();
    }

    @Pattern
    private static BufferBuilder Tessellator_getBuffer(Tesselator tessellator) {
        return tessellator.getBuilder();
    }

    @Pattern
    private static void BufferBuilder_beginPosCol(BufferBuilder buffer, VertexFormat.Mode mode) {
        buffer.begin(mode, DefaultVertexFormat.POSITION_COLOR);
    }

    @Pattern
    private static void BufferBuilder_addPosCol(BufferBuilder buffer, double x, double y, double z, int r, int g, int b, int a) {
        buffer.vertex(x, y, z).color(r, g, b, a).endVertex();
    }

    @Pattern
    private static void BufferBuilder_beginPosTex(BufferBuilder buffer, VertexFormat.Mode mode) {
        buffer.begin(mode, DefaultVertexFormat.POSITION_TEX);
    }

    @Pattern
    private static void BufferBuilder_addPosTex(BufferBuilder buffer, double x, double y, double z, float u, float v) {
        buffer.vertex(x, y, z).uv(u, v).endVertex();
    }

    @Pattern
    private static void BufferBuilder_beginPosTexCol(BufferBuilder buffer, VertexFormat.Mode mode) {
        buffer.begin(mode, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    @Pattern
    private static void BufferBuilder_addPosTexCol(BufferBuilder buffer, double x, double y, double z, float u, float v, int r, int g, int b, int a) {
        buffer.vertex(x, y, z).uv(u, v).color(r, g, b, a).endVertex();
    }

    @Pattern
    private static Tesselator Tessellator_getInstance() {
        return Tesselator.getInstance();
    }

    @Pattern
    private static EntityRenderDispatcher getEntityRenderDispatcher(Minecraft mc) {
        return mc.getEntityRenderDispatcher();
    }

    @Pattern
    private static float getCameraYaw(EntityRenderDispatcher dispatcher) {
        return dispatcher.camera.getYRot();
    }

    @Pattern
    private static float getCameraPitch(EntityRenderDispatcher dispatcher) {
        return dispatcher.camera.getXRot();
    }

    @Pattern
    private static float getRenderPartialTicks(Minecraft mc) {
        return mc.getPartialTick();
    }

    @Pattern
    private static TextureManager getTextureManager(Minecraft mc) {
        return mc.getTextureManager();
    }

    @Pattern
    private static String getBoundKeyName(KeyMapping keyBinding) {
        return keyBinding.getTranslatedKeyMessage().getString();
    }

    @Pattern
    private static SimpleSoundInstance master(ResourceLocation sound, float pitch) {
        return SimpleSoundInstance.forUI(new SoundEvent(sound), pitch);
    }

    @Pattern
    private static boolean isKeyMappingConflicting(KeyMapping a, KeyMapping b) {
        return a.same(b);
    }
}

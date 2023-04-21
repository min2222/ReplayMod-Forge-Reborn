package com.replaymod.simplepathing.preview;

import static com.replaymod.core.ReplayMod.TEXTURE;

import java.util.Comparator;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.events.PostRenderWorldCallback;
import com.replaymod.core.versions.MCVer;
import com.replaymod.gui.utils.EventRegistrations;
import com.replaymod.pathing.properties.CameraProperties;
import com.replaymod.pathing.properties.SpectatorProperty;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.util.EntityPositionTracker;
import com.replaymod.replaystudio.util.Location;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import com.replaymod.simplepathing.SPTimeline;
import com.replaymod.simplepathing.gui.GuiPathing;

import de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class PathPreviewRenderer extends EventRegistrations {
    private static final ResourceLocation CAMERA_HEAD = new ResourceLocation("replaymod", "camera_head.png");
    private static final Minecraft mc = MCVer.getMinecraft();

    private static final int SLOW_PATH_COLOR = 0xffcccc;
    private static final int FAST_PATH_COLOR = 0x660000;
    private static final double FASTEST_PATH_SPEED = 0.01;

    private final ReplayModSimplePathing mod;
    private final ReplayHandler replayHandler;

    public PathPreviewRenderer(ReplayModSimplePathing mod, ReplayHandler replayHandler) {
        this.mod = mod;
        this.replayHandler = replayHandler;
    }

    {
        on(PostRenderWorldCallback.EVENT, this::renderCameraPath);
    }

    private void renderCameraPath(PoseStack matrixStack) {
        if (!replayHandler.getReplaySender().isAsyncMode() || mc.options.hideGui) return;

        Entity view = mc.getCameraEntity();
        if (view == null) return;

        GuiPathing guiPathing = mod.getGuiPathing();
        if (guiPathing == null) return;
        EntityPositionTracker entityTracker = guiPathing.getEntityTracker();

        SPTimeline timeline = mod.getCurrentTimeline();
        if (timeline == null) return;
        Path path = timeline.getPositionPath();
        if (path.getKeyframes().isEmpty()) return;
        Path timePath = timeline.getTimePath();

        path.update();

        int renderDistance = mc.options.renderDistance().get() * 16;
        int renderDistanceSquared = renderDistance * renderDistance;

        Vector3f viewPos = new Vector3f((float)view.getX(), (float)view.getY(), (float)view.getZ());

        matrixStack.pushPose();
        try {
            RenderSystem.setProjectionMatrix(matrixStack.last().pose());
            RenderSystem.applyModelViewMatrix();
            for (PathSegment segment : path.getSegments()) {
                Interpolator interpolator = segment.getInterpolator();
                Keyframe start = segment.getStartKeyframe();
                Keyframe end = segment.getEndKeyframe();
                long diff = (int) (end.getTime() - start.getTime());

                boolean spectator = interpolator.getKeyframeProperties().contains(SpectatorProperty.PROPERTY);
                if (spectator && entityTracker == null) {
                    continue; // Cannot render spectator positions when entity tracker is not yet loaded
                }
                // Spectator segments have 20 lines per second (at least 10) whereas normal segments have a fixed 100
                long steps = spectator ? Math.max(diff / 50, 10) : 100;
                Vector3f prevPos = null;
                for (int i = 0; i <= steps; i++) {
                    long time = start.getTime() + diff * i / steps;
                    if (spectator) {
                        Optional<Integer> entityId = path.getValue(SpectatorProperty.PROPERTY, time);
                        Optional<Integer> replayTime = timePath.getValue(TimestampProperty.PROPERTY, time);
                        if (entityId.isPresent() && replayTime.isPresent()) {
                            Location loc = entityTracker.getEntityPositionAtTimestamp(entityId.get(), replayTime.get());
                            if (loc != null) {
                            	Vector3f pos = loc2Vec(loc);
                                if (prevPos != null) {
                                    drawConnection(viewPos, prevPos, pos, 0x0000ff, renderDistanceSquared);
                                }
                                prevPos = pos;
                                continue;
                            }
                        }
                    } else {
                    	Optional<Vector3f> optPos = path.getValue(CameraProperties.POSITION, time).map(this::tripleD2Vec);
                        if (optPos.isPresent()) {
                        	Vector3f pos = optPos.get();
                            if (prevPos != null) {
                                double distance = Math.sqrt(distanceSquared(prevPos, pos));
                                double speed = Math.min(distance / (diff / steps), FASTEST_PATH_SPEED);
                                double speedFraction = speed / FASTEST_PATH_SPEED;
                                int color = interpolateColor(SLOW_PATH_COLOR, FAST_PATH_COLOR, speedFraction);
                                drawConnection(viewPos, prevPos, pos, color, renderDistanceSquared);
                            }
                            prevPos = pos;
                            continue;
                        }
                    }
                    prevPos = null;
                }
            }

            GL11.glEnable(3042);
            GL11.glBlendFunc(774, 768);
            GL11.glDisable(2929);
            path.getKeyframes().stream()
            .map(k -> Pair.of(k, k.getValue(CameraProperties.POSITION).map(this::tripleD2Vec)))
            .filter(p -> (p.getRight()).isPresent())
            .map(p -> Pair.of(p.getLeft(), ((Optional<Vector3f>)p.getRight()).get()))
            .filter(p -> (distanceSquared(p.getRight(), viewPos) < renderDistanceSquared))
            .sorted(new KeyframeComparator(viewPos))
            .forEachOrdered(p -> drawPoint(viewPos, (Vector3f)p.getRight(), (Keyframe)p.getLeft()));
            
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(2929);
            int time = guiPathing.timeline.getCursorPosition();
            Optional<Integer> entityId = path.getValue(SpectatorProperty.PROPERTY, time);
            if (entityId.isPresent()) {
                // Spectating an entity
                if (entityTracker != null) {
                    Optional<Integer> replayTime = timePath.getValue(TimestampProperty.PROPERTY, time);
                    if (replayTime.isPresent()) {
                        Location loc = entityTracker.getEntityPositionAtTimestamp(entityId.get(), replayTime.get());
                        if (loc != null) {
                        	drawCamera(viewPos, loc2Vec(loc), new Vector3f(loc.getYaw(), loc.getPitch(), 0.0F)); 
                        }
                    }
                }
            } else {
                // Normal camera path
                Optional<Vector3f> cameraPos = path.getValue(CameraProperties.POSITION, time).map(this::tripleD2Vec);
                Optional<Vector3f> cameraRot = path.getValue(CameraProperties.ROTATION, time).map(this::tripleF2Vec);
                if (cameraPos.isPresent() && cameraRot.isPresent()) {
                    drawCamera(viewPos, cameraPos.get(), cameraRot.get());
                }
            }
        } finally {
            matrixStack.popPose();
            GL11.glDisable(3042);
        }
    }
    
    private Vector3f loc2Vec(Location loc) {
        return new Vector3f((float)loc.getX(), (float)loc.getY(), (float)loc.getZ());
    }
    
    private Vector3f tripleF2Vec(Triple<Float, Float, Float> loc) {
        return new Vector3f(((Float)loc.getLeft()).floatValue(), ((Float)loc.getMiddle()).floatValue(), ((Float)loc.getRight()).floatValue());
    }
    
    private Vector3f tripleD2Vec(Triple<Double, Double, Double> loc) {
        return new Vector3f(((Double)loc.getLeft()).floatValue(), ((Double)loc.getMiddle()).floatValue(), ((Double)loc.getRight()).floatValue());
      }

    private static int interpolateColor(int c1, int c2, double weight) {
        return (interpolateColorComponent((c1 >> 16) & 0xff, (c2 >> 16) & 0xff, weight) << 16)
                | (interpolateColorComponent((c1 >> 8) & 0xff, (c2 >> 8) & 0xff, weight) << 8)
                | interpolateColorComponent(c1 & 0xff, c2 & 0xff, weight);
    }

    private static int interpolateColorComponent(int c1, int c2, double weight) {
        return (int) (c1 + (1 - Math.pow(Math.E, -4 * weight)) * (c2 - c1)) & 0xff;
    }
    
    private static double distanceSquared(Vector3f p1, Vector3f p2) {
        return Vector3f.sub(p1, p2, null).lengthSquared();
    }

    private void drawConnection(Vector3f view, Vector3f pos1, Vector3f pos2,
                                int color, int renderDistanceSquared) {
        if (distanceSquared(view, pos1) > renderDistanceSquared) return;
        if (distanceSquared(view, pos2) > renderDistanceSquared) return;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        MCVer.emitLine(buffer, Vector3f.sub(pos1, view, null), Vector3f.sub(pos2, view, null), color);
        
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.disableCull();
        RenderSystem.lineWidth(3.0F);
        tessellator.end();
        RenderSystem.enableCull();
        //TODO path rendering is broken
    }

    private void drawPoint(Vector3f view,
    		Vector3f pos,
                           Keyframe keyframe) {

        RenderSystem.setShaderTexture(0, TEXTURE);

        float posX = 80f / ReplayMod.TEXTURE_SIZE;
        float posY = 0f;
        float size = 10f / ReplayMod.TEXTURE_SIZE;

        if (mod.isSelected(keyframe)) {
            posY += size;
        }

        if (keyframe.getValue(SpectatorProperty.PROPERTY).isPresent()) {
            posX += size;
        }

        float minX = -0.5f;
        float minY = -0.5f;
        float maxX = 0.5f;
        float maxY = 0.5f;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buffer.vertex(minX, minY, 0).uv(posX + size, posY + size).endVertex();
        buffer.vertex(minX, maxY, 0).uv(posX + size, posY).endVertex();
        buffer.vertex(maxX, maxY, 0).uv(posX, posY).endVertex();
        buffer.vertex(maxX, minY, 0).uv(posX, posY + size).endVertex();
        
        RenderSystem.getModelViewStack().pushPose();
        Vector3f t = Vector3f.sub(pos, view, null);
        RenderSystem.getModelViewStack().translate(t.x, t.y, t.z);
        RenderSystem.getModelViewStack().mulPose(MCVer.quaternion(-(mc.getEntityRenderDispatcher()).camera.getYRot(), new com.mojang.math.Vector3f(0.0F, 1.0F, 0.0F)));
        RenderSystem.getModelViewStack().mulPose(MCVer.quaternion((mc.getEntityRenderDispatcher()).camera.getXRot(), new com.mojang.math.Vector3f(1.0F, 0.0F, 0.0F)));
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        tessellator.end();
        RenderSystem.getModelViewStack().popPose();
    }

    private void drawCamera(Vector3f view, Vector3f pos, Vector3f rot) {

        RenderSystem.setShaderTexture(0, CAMERA_HEAD);
        RenderSystem.getModelViewStack().pushPose();
        Vector3f t = Vector3f.sub(pos, view, null);
        RenderSystem.getModelViewStack().translate(t.x, t.y, t.z);
        RenderSystem.getModelViewStack().mulPose(MCVer.quaternion(-rot.x, new com.mojang.math.Vector3f(0.0F, 1.0F, 0.0F)));
        RenderSystem.getModelViewStack().mulPose(MCVer.quaternion(rot.y, new com.mojang.math.Vector3f(1.0F, 0.0F, 0.0F)));
        RenderSystem.getModelViewStack().mulPose(MCVer.quaternion(rot.z, new com.mojang.math.Vector3f(0.0F, 0.0F, 1.0F)));
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        MCVer.emitLine(buffer, new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 2.0F), 16711850);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        tessellator.end();
        float cubeSize = 0.5f;

        double r = -cubeSize / 2;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        //back
        buffer.vertex(r, r + cubeSize, r).uv(3 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r + cubeSize, r).uv(4 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r, r).uv(4 * 8 / 64f, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r, r).uv(3 * 8 / 64f, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();

        //front
        buffer.vertex(r + cubeSize, r, r + cubeSize).uv(2 * 8 / 64f, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r + cubeSize, r + cubeSize).uv(2 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r + cubeSize, r + cubeSize).uv(8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r, r + cubeSize).uv(8 / 64f, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();

        //left
        buffer.vertex(r + cubeSize, r + cubeSize, r).uv(0, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r + cubeSize, r + cubeSize).uv(8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r, r + cubeSize).uv(8 / 64f, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r, r).uv(0, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();

        //right
        buffer.vertex(r, r + cubeSize, r + cubeSize).uv(2 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r + cubeSize, r).uv(3 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r, r).uv(3 * 8 / 64f, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r, r + cubeSize).uv(2 * 8 / 64f, 2 * 8 / 64f).color(255, 255, 255, 200).endVertex();

        //bottom
        buffer.vertex(r + cubeSize, r, r).uv(3 * 8 / 64f, 0).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r, r + cubeSize).uv(3 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r, r + cubeSize).uv(2 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r, r).uv(2 * 8 / 64f, 0).color(255, 255, 255, 200).endVertex();

        //top
        buffer.vertex(r, r + cubeSize, r).uv(8 / 64f, 0).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r, r + cubeSize, r + cubeSize).uv(8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r + cubeSize, r + cubeSize).uv(2 * 8 / 64f, 8 / 64f).color(255, 255, 255, 200).endVertex();
        buffer.vertex(r + cubeSize, r + cubeSize, r).uv(2 * 8 / 64f, 0).color(255, 255, 255, 200).endVertex();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        tessellator.end();

        RenderSystem.getModelViewStack().popPose();
    }

    
    private class KeyframeComparator implements Comparator<Pair<Keyframe, Vector3f>> {
    	private final Vector3f viewPos;
      
    	public KeyframeComparator(Vector3f viewPos) {
    		this.viewPos = viewPos;
    	}
      
    	@Override
    	public int compare(Pair<Keyframe, Vector3f> o1, Pair<Keyframe, Vector3f> o2) {
    		return -Double.compare(PathPreviewRenderer.distanceSquared(o1.getRight(), this.viewPos), PathPreviewRenderer.distanceSquared(o2.getRight(), this.viewPos));
    	}
    }
}

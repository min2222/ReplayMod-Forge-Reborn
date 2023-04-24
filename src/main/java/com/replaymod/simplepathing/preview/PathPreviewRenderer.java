package com.replaymod.simplepathing.preview;

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

	private static final int SLOW_PATH_COLOR = 16764108;

	private static final int FAST_PATH_COLOR = 6684672;

	private static final double FASTEST_PATH_SPEED = 0.01D;

	private final ReplayModSimplePathing mod;

	private final ReplayHandler replayHandler;

	public PathPreviewRenderer(ReplayModSimplePathing mod, ReplayHandler replayHandler) {
		on(PostRenderWorldCallback.EVENT, this::renderCameraPath);
		this.mod = mod;
		this.replayHandler = replayHandler;
	}

	private void renderCameraPath(PoseStack matrixStack) {
		if (!this.replayHandler.getReplaySender().isAsyncMode() || mc.options.hideGui)
			return;
		Entity view = mc.getCameraEntity();
		if (view == null)
			return;
		GuiPathing guiPathing = this.mod.getGuiPathing();
		if (guiPathing == null)
			return;
		EntityPositionTracker entityTracker = guiPathing.getEntityTracker();
		SPTimeline timeline = this.mod.getCurrentTimeline();
		if (timeline == null)
			return;
		Path path = timeline.getPositionPath();
		if (path.getKeyframes().isEmpty())
			return;
		Path timePath = timeline.getTimePath();
		path.update();
		int renderDistance = ((Integer) mc.options.renderDistance().get()).intValue() * 16;
		int renderDistanceSquared = renderDistance * renderDistance;
		Vector3f viewPos = new Vector3f((float) view.getX(), (float) view.getY(), (float) view.getZ());
		MCVer.pushMatrix();
		try {
			RenderSystem.getModelViewStack().mulPoseMatrix(matrixStack.last().pose());
			RenderSystem.applyModelViewMatrix();
			for (PathSegment segment : path.getSegments()) {
				Interpolator interpolator = segment.getInterpolator();
				Keyframe start = segment.getStartKeyframe();
				Keyframe end = segment.getEndKeyframe();
				long diff = (int) (end.getTime() - start.getTime());
				boolean spectator = interpolator.getKeyframeProperties().contains(SpectatorProperty.PROPERTY);
				if (spectator && entityTracker == null)
					continue;
				long steps = spectator ? Math.max(diff / 50L, 10L) : 100L;
				Vector3f prevPos = null;
				for (int i = 0; i <= steps; i++) {
					long l = start.getTime() + diff * i / steps;
					if (spectator) {
						Optional<Integer> optional1 = path.getValue(SpectatorProperty.PROPERTY, l);
						Optional<Integer> replayTime = timePath.getValue(TimestampProperty.PROPERTY, l);
						if (optional1.isPresent() && replayTime.isPresent()) {
							Location loc = entityTracker.getEntityPositionAtTimestamp(
									((Integer) optional1.get()).intValue(), ((Integer) replayTime.get()).intValue());
							if (loc != null) {
								Vector3f pos = loc2Vec(loc);
								if (prevPos != null)
									drawConnection(viewPos, prevPos, pos, 65535, renderDistanceSquared);
								prevPos = pos;
								continue;
							}
						}
					} else {
						Optional<Vector3f> optPos = path.getValue(CameraProperties.POSITION, l).map(this::tripleD2Vec);
						if (optPos.isPresent()) {
							Vector3f pos = optPos.get();
							if (prevPos != null) {
								double distance = Math.sqrt(distanceSquared(prevPos, pos));
								double speed = Math.min(distance / (diff / steps), 0.01D);
								double speedFraction = speed / 0.01D;
								int color = interpolateColor(16764108, 6684672, speedFraction);
								drawConnection(viewPos, prevPos, pos, color << 8 | 0xFF, renderDistanceSquared);
							}
							prevPos = pos;
							continue;
						}
					}
					prevPos = null;
					continue;
				}
			}
			GL11.glEnable(3042);
			GL11.glBlendFunc(774, 768);
			GL11.glDisable(2929);
			path.getKeyframes().stream()
					.map(k -> Pair.of(k, k.getValue(CameraProperties.POSITION).map(this::tripleD2Vec)))
					.filter(p -> (p.getRight()).isPresent())
					.map(p -> Pair.of(p.getLeft(), ((Optional<Vector3f>) p.getRight()).get()))
					.filter(p -> (distanceSquared((Vector3f) p.getRight(), viewPos) < renderDistanceSquared))
					.sorted(new KeyframeComparator(viewPos))
					.forEachOrdered(p -> drawPoint(viewPos, (Vector3f) p.getRight(), (Keyframe) p.getLeft()));
			GL11.glBlendFunc(770, 771);
			GL11.glEnable(2929);
			int time = guiPathing.timeline.getCursorPosition();
			Optional<Integer> entityId = path.getValue(SpectatorProperty.PROPERTY, time);
			if (entityId.isPresent()) {
				if (entityTracker != null) {
					Optional<Integer> replayTime = timePath.getValue(TimestampProperty.PROPERTY, time);
					if (replayTime.isPresent()) {
						Location loc = entityTracker.getEntityPositionAtTimestamp(((Integer) entityId.get()).intValue(),
								((Integer) replayTime.get()).intValue());
						if (loc != null)
							drawCamera(viewPos, loc2Vec(loc), new Vector3f(loc.getYaw(), loc.getPitch(), 0.0F));
					}
				}
			} else {
				Optional<Vector3f> cameraPos = path.getValue(CameraProperties.POSITION, time).map(this::tripleD2Vec);
				Optional<Vector3f> cameraRot = path.getValue(CameraProperties.ROTATION, time).map(this::tripleF2Vec);
				if (cameraPos.isPresent() && cameraRot.isPresent())
					drawCamera(viewPos, cameraPos.get(), cameraRot.get());
			}
		} finally {
			MCVer.popMatrix();
			GL11.glDisable(3042);
		}
	}

	private Vector3f loc2Vec(Location loc) {
		return new Vector3f((float) loc.getX(), (float) loc.getY(), (float) loc.getZ());
	}

	private Vector3f tripleD2Vec(Triple<Double, Double, Double> loc) {
		return new Vector3f(((Double) loc.getLeft()).floatValue(), ((Double) loc.getMiddle()).floatValue(),
				((Double) loc.getRight()).floatValue());
	}

	private Vector3f tripleF2Vec(Triple<Float, Float, Float> loc) {
		return new Vector3f(((Float) loc.getLeft()).floatValue(), ((Float) loc.getMiddle()).floatValue(),
				((Float) loc.getRight()).floatValue());
	}

	private static int interpolateColor(int c1, int c2, double weight) {
		return interpolateColorComponent(c1 >> 16 & 0xFF, c2 >> 16 & 0xFF, weight) << 16
				| interpolateColorComponent(c1 >> 8 & 0xFF, c2 >> 8 & 0xFF, weight) << 8
				| interpolateColorComponent(c1 & 0xFF, c2 & 0xFF, weight);
	}

	private static int interpolateColorComponent(int c1, int c2, double weight) {
		return (int) (c1 + (1.0D - Math.pow(Math.E, -4.0D * weight)) * (c2 - c1)) & 0xFF;
	}

	private static double distanceSquared(Vector3f p1, Vector3f p2) {
		return Vector3f.sub(p1, p2, null).lengthSquared();
	}

	private void drawConnection(Vector3f view, Vector3f pos1, Vector3f pos2, int color, int renderDistanceSquared) {
		if (distanceSquared(view, pos1) > renderDistanceSquared)
			return;
		if (distanceSquared(view, pos2) > renderDistanceSquared)
			return;
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
		MCVer.emitLine(buffer, Vector3f.sub(pos1, view, null), Vector3f.sub(pos2, view, null), color);
		RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
		RenderSystem.disableCull();
		RenderSystem.lineWidth(3.0F);
		tessellator.end();
		RenderSystem.enableCull();
	}

	private void drawPoint(Vector3f view, Vector3f pos, Keyframe keyframe) {
		MCVer.bindTexture(ReplayMod.TEXTURE);
		float posX = 0.3125F;
		float posY = 0.0F;
		float size = 0.0390625F;
		if (this.mod.isSelected(keyframe))
			posY += size;
		if (keyframe.getValue(SpectatorProperty.PROPERTY).isPresent())
			posX += size;
		float minX = -0.5F;
		float minY = -0.5F;
		float maxX = 0.5F;
		float maxY = 0.5F;
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex(minX, minY, 0.0D).uv(posX + size, posY + size).endVertex();
		buffer.vertex(minX, maxY, 0.0D).uv(posX + size, posY).endVertex();
		buffer.vertex(maxX, maxY, 0.0D).uv(posX, posY).endVertex();
		buffer.vertex(maxX, minY, 0.0D).uv(posX, posY + size).endVertex();
		MCVer.pushMatrix();
		Vector3f t = Vector3f.sub(pos, view, null);
		RenderSystem.getModelViewStack().translate(t.x, t.y, t.z);
		RenderSystem.getModelViewStack().mulPose(MCVer.quaternion(-(mc.getEntityRenderDispatcher()).camera.getYRot(),
				new com.mojang.math.Vector3f(0.0F, 1.0F, 0.0F)));
		RenderSystem.getModelViewStack().mulPose(MCVer.quaternion((mc.getEntityRenderDispatcher()).camera.getXRot(),
				new com.mojang.math.Vector3f(1.0F, 0.0F, 0.0F)));
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		tessellator.end();
		MCVer.popMatrix();
	}

	private void drawCamera(Vector3f view, Vector3f pos, Vector3f rot) {
		MCVer.bindTexture(CAMERA_HEAD);
		MCVer.pushMatrix();
		Vector3f t = Vector3f.sub(pos, view, null);
		RenderSystem.getModelViewStack().translate(t.x, t.y, t.z);
		RenderSystem.getModelViewStack()
				.mulPose(MCVer.quaternion(-rot.x, new com.mojang.math.Vector3f(0.0F, 1.0F, 0.0F)));
		RenderSystem.getModelViewStack()
				.mulPose(MCVer.quaternion(rot.y, new com.mojang.math.Vector3f(1.0F, 0.0F, 0.0F)));
		RenderSystem.getModelViewStack()
				.mulPose(MCVer.quaternion(rot.z, new com.mojang.math.Vector3f(0.0F, 0.0F, 1.0F)));
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
		MCVer.emitLine(buffer, new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 2.0F), 16711850);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
		tessellator.end();
		float cubeSize = 0.5F;
		double r = (-cubeSize / 2.0F);
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		buffer.vertex(r, r + cubeSize, r).uv(0.375F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r + cubeSize, r).uv(0.5F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r, r).uv(0.5F, 0.25F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r, r).uv(0.375F, 0.25F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r, r + cubeSize).uv(0.25F, 0.25F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r + cubeSize, r + cubeSize).uv(0.25F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r + cubeSize, r + cubeSize).uv(0.125F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r, r + cubeSize).uv(0.125F, 0.25F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r + cubeSize, r).uv(0.0F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r + cubeSize, r + cubeSize).uv(0.125F, 0.125F).color(255, 255, 255, 200)
				.endVertex();
		buffer.vertex(r + cubeSize, r, r + cubeSize).uv(0.125F, 0.25F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r, r).uv(0.0F, 0.25F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r + cubeSize, r + cubeSize).uv(0.25F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r + cubeSize, r).uv(0.375F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r, r).uv(0.375F, 0.25F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r, r + cubeSize).uv(0.25F, 0.25F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r, r).uv(0.375F, 0.0F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r, r + cubeSize).uv(0.375F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r, r + cubeSize).uv(0.25F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r, r).uv(0.25F, 0.0F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r + cubeSize, r).uv(0.125F, 0.0F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r, r + cubeSize, r + cubeSize).uv(0.125F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r + cubeSize, r + cubeSize).uv(0.25F, 0.125F).color(255, 255, 255, 200).endVertex();
		buffer.vertex(r + cubeSize, r + cubeSize, r).uv(0.25F, 0.0F).color(255, 255, 255, 200).endVertex();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		tessellator.end();
		MCVer.popMatrix();
	}

	private class KeyframeComparator implements Comparator<Pair<Keyframe, Vector3f>> {
		private final Vector3f viewPos;

		public KeyframeComparator(Vector3f viewPos) {
			this.viewPos = viewPos;
		}

		public int compare(Pair<Keyframe, Vector3f> o1, Pair<Keyframe, Vector3f> o2) {
			return -Double.compare(PathPreviewRenderer.distanceSquared((Vector3f) o1.getRight(), this.viewPos),
					PathPreviewRenderer.distanceSquared((Vector3f) o2.getRight(), this.viewPos));
		}
	}
}

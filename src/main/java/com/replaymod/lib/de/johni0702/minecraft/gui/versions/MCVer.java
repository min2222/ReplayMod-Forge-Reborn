package com.replaymod.lib.de.johni0702.minecraft.gui.versions;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MCVer {
	private static final ArrayDeque<MCVer.ScissorBounds> scissorStateStack = new ArrayDeque();
	private static MCVer.ScissorBounds scissorState;

	public static Minecraft getMinecraft() {
		return Minecraft.getInstance();
	}

	public static void pushScissorState() {
		scissorStateStack.push(scissorState);
	}

	public static void popScissorState() {
		setScissorBounds((MCVer.ScissorBounds) scissorStateStack.pop());
	}

	public static void setScissorBounds(int x, int y, int width, int height) {
		setScissorBounds(new MCVer.ScissorBounds(x, y, width, height));
	}

	public static void setScissorDisabled() {
		setScissorBounds(MCVer.ScissorBounds.DISABLED);
	}

	private static void setScissorBounds(MCVer.ScissorBounds newState) {
		MCVer.ScissorBounds oldState = scissorState;
		if (!Objects.equals(oldState, newState)) {
			scissorState = newState;
			boolean isEnabled = newState != MCVer.ScissorBounds.DISABLED;
			boolean wasEnabled = oldState != MCVer.ScissorBounds.DISABLED;
			if (isEnabled) {
				if (!wasEnabled) {
					GL11.glEnable(3089);
				}

				GL11.glScissor(scissorState.x, scissorState.y, scissorState.width, scissorState.height);
			} else {
				GL11.glDisable(3089);
			}

		}
	}

	public static Window newScaledResolution(Minecraft mc) {
		return mc.getWindow();
	}

	public static void addDetail(CrashReportCategory category, String name, Callable<String> callable) {
		Objects.requireNonNull(callable);
		category.setDetail(name, callable::call);
	}

	public static void drawRect(int right, int bottom, int left, int top) {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder vertexBuffer = tessellator.getBuilder();
		vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		vertexBuffer.vertex((double) right, (double) top, 0.0D).endVertex();
		vertexBuffer.vertex((double) left, (double) top, 0.0D).endVertex();
		vertexBuffer.vertex((double) left, (double) bottom, 0.0D).endVertex();
		vertexBuffer.vertex((double) right, (double) bottom, 0.0D).endVertex();
		tessellator.end();
	}

	public static void drawRect(int x, int y, int width, int height, ReadableColor tl, ReadableColor tr,
			ReadableColor bl, ReadableColor br) {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder vertexBuffer = tessellator.getBuilder();
		vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		vertexBuffer.vertex((double) x, (double) (y + height), 0.0D)
				.color(bl.getRed(), bl.getGreen(), bl.getBlue(), bl.getAlpha()).endVertex();
		vertexBuffer.vertex((double) (x + width), (double) (y + height), 0.0D)
				.color(br.getRed(), br.getGreen(), br.getBlue(), br.getAlpha()).endVertex();
		vertexBuffer.vertex((double) (x + width), (double) y, 0.0D)
				.color(tr.getRed(), tr.getGreen(), tr.getBlue(), tr.getAlpha()).endVertex();
		vertexBuffer.vertex((double) x, (double) y, 0.0D).color(tl.getRed(), tl.getGreen(), tl.getBlue(), tl.getAlpha())
				.endVertex();
		tessellator.end();
	}

	public static void bindTexture(ResourceLocation identifier) {
		RenderSystem.enableTexture();
		RenderSystem.setShaderTexture(0, identifier);
	}

	public static Font getFontRenderer() {
		return getMinecraft().font;
	}

	public static void setClipboardString(String text) {
		getMinecraft().keyboardHandler.setClipboard(text);
	}

	public static String getClipboardString() {
		return getMinecraft().keyboardHandler.getClipboard();
	}

	public static Component literalText(String str) {
		return Component.literal(str);
	}

	static {
		scissorState = MCVer.ScissorBounds.DISABLED;
	}

	private static class ScissorBounds {
		private static final MCVer.ScissorBounds DISABLED = new MCVer.ScissorBounds(0, 0, Integer.MAX_VALUE,
				Integer.MAX_VALUE);
		private final int x;
		private final int y;
		private final int width;
		private final int height;

		private ScissorBounds(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o != null && this.getClass() == o.getClass()) {
				MCVer.ScissorBounds that = (MCVer.ScissorBounds) o;
				return this.x == that.x && this.y == that.y && this.width == that.width && this.height == that.height;
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[] { this.x, this.y, this.width, this.height });
		}
	}

	public abstract static class Keyboard {
		public static final int KEY_ESCAPE = 256;
		public static final int KEY_HOME = 268;
		public static final int KEY_END = 269;
		public static final int KEY_UP = 265;
		public static final int KEY_DOWN = 264;
		public static final int KEY_LEFT = 263;
		public static final int KEY_RIGHT = 262;
		public static final int KEY_BACK = 259;
		public static final int KEY_DELETE = 261;
		public static final int KEY_RETURN = 257;
		public static final int KEY_TAB = 258;
		public static final int KEY_A = 65;
		public static final int KEY_C = 67;
		public static final int KEY_V = 86;
		public static final int KEY_X = 88;

		public static void enableRepeatEvents(boolean enabled) {
			MCVer.getMinecraft().keyboardHandler.setSendRepeatsToGui(enabled);
		}
	}
}

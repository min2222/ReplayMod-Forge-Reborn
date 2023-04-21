package com.replaymod.gui.versions;

import java.util.concurrent.Callable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.client.event.ScreenEvent;

/**
 * Abstraction over things that have changed between different MC versions.
 */
public class MCVer {
    public static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public static Window newScaledResolution(Minecraft mc) {
        return mc.getWindow();
    }

    public static void addDetail(CrashReportCategory category, String name, Callable<String> callable) {
        category.setDetail(name, callable::call);
    }

    public static void drawRect(int right, int bottom, int left, int top) {
    	Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        vertexBuffer.vertex(right, top, 0).endVertex();
        vertexBuffer.vertex(left, top, 0).endVertex();
        vertexBuffer.vertex(left, bottom, 0).endVertex();
        vertexBuffer.vertex(right, bottom, 0).endVertex();
        tessellator.end();
    }

    public static void drawRect(int x, int y, int width, int height, ReadableColor tl, ReadableColor tr, ReadableColor bl, ReadableColor br) {
    	Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuilder();
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertexBuffer.vertex(x, y + height, 0).color(bl.getRed(), bl.getGreen(), bl.getBlue(), bl.getAlpha()).endVertex();
        vertexBuffer.vertex(x + width, y + height, 0).color(br.getRed(), br.getGreen(), br.getBlue(), br.getAlpha()).endVertex();
        vertexBuffer.vertex(x + width, y, 0).color(tr.getRed(), tr.getGreen(), tr.getBlue(), tr.getAlpha()).endVertex();
        vertexBuffer.vertex(x, y, 0).color(tl.getRed(), tl.getGreen(), tl.getBlue(), tl.getAlpha()).endVertex();
        tessellator.end();
    }

    public static Font getFontRenderer() {
        return getMinecraft().font;
    }

    public static int getMouseX(ScreenEvent.Render.Post event) {
        return event.getMouseX();
    }

    public static int getMouseY(ScreenEvent.Render.Post event) {
        return event.getMouseY();
    }

    public static void setClipboardString(String text) {
        getMinecraft().keyboardHandler.setClipboard(text);
    }

    public static String getClipboardString() {
        return getMinecraft().keyboardHandler.getClipboard();
    }


    public static abstract class Keyboard {
        public static final int KEY_ESCAPE = GLFW.GLFW_KEY_ESCAPE;
        public static final int KEY_HOME = GLFW.GLFW_KEY_HOME;
        public static final int KEY_END = GLFW.GLFW_KEY_END;
        public static final int KEY_UP = GLFW.GLFW_KEY_UP;
        public static final int KEY_DOWN = GLFW.GLFW_KEY_DOWN;
        public static final int KEY_LEFT = GLFW.GLFW_KEY_LEFT;
        public static final int KEY_RIGHT = GLFW.GLFW_KEY_RIGHT;
        public static final int KEY_BACK = GLFW.GLFW_KEY_BACKSPACE;
        public static final int KEY_DELETE = GLFW.GLFW_KEY_DELETE;
        public static final int KEY_RETURN = GLFW.GLFW_KEY_ENTER;
        public static final int KEY_TAB = GLFW.GLFW_KEY_TAB;
        public static final int KEY_A = GLFW.GLFW_KEY_A;
        public static final int KEY_C = GLFW.GLFW_KEY_C;
        public static final int KEY_V = GLFW.GLFW_KEY_V;
        public static final int KEY_X = GLFW.GLFW_KEY_X;

        public static void enableRepeatEvents(boolean enabled) {
            getMinecraft().keyboardHandler.setSendRepeatsToGui(enabled);
        }
    }
}

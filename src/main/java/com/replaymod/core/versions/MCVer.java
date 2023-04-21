package com.replaymod.core.versions;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Quaternion;
import com.replaymod.core.MinecraftMethodAccessor;
import com.replaymod.mixin.MainWindowAccessor;
import com.replaymod.mixin.ParticleAccessor;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.packet.State;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.version.ProtocolVersion;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;

import de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector2f;
import de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.Particle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Abstraction over things that have changed between different MC versions.
 */
public class MCVer {
    public static int getProtocolVersion() {
        return SharedConstants.getCurrentVersion().getProtocolVersion();
    }

    public static PacketTypeRegistry getPacketTypeRegistry(boolean loginPhase) {
        return PacketTypeRegistry.get(
                ProtocolVersion.getProtocol(getProtocolVersion()),
                loginPhase ? State.LOGIN : State.PLAY
        );
    }

    public static void resizeMainWindow(Minecraft mc, int width, int height) {
        RenderTarget fb = mc.getMainRenderTarget();
        if (fb.width != width || fb.height != height) {
            fb.resize(width, height, false);
        }
        //noinspection ConstantConditions
        MainWindowAccessor mainWindow = (MainWindowAccessor) (Object) mc.getWindow();
        mainWindow.setFramebufferWidth(width);
        mainWindow.setFramebufferHeight(height);
        mc.gameRenderer.resize(width, height);
    }
    
    public static void emitLine(BufferBuilder buffer, Vector2f p1, Vector2f p2, int color) {
        emitLine(buffer, new Vector3f(p1.x, p1.y, 0.0F), new Vector3f(p2.x, p2.y, 0.0F), color);
      }
      
      public static void emitLine(BufferBuilder buffer, Vector3f p1, Vector3f p2, int color) {
        int r = color >> 24 & 0xFF;
        int g = color >> 16 & 0xFF;
        int b = color >> 8 & 0xFF;
        int a = color & 0xFF;
        Vector3f n = Vector3f.sub(p2, p1, null);
        buffer.vertex(p1.x, p1.y, p1.z)
          .color(r, g, b, a)
          
          .normal(n.x, n.y, n.z)
          
          .endVertex();
        buffer.vertex(p2.x, p2.y, p2.z)
          .color(r, g, b, a)
          
          .normal(n.x, n.y, n.z)
          
          .endVertex();
      }
    
    public static Quaternion quaternion(float angle, com.mojang.math.Vector3f axis) {
        return new Quaternion(axis, angle, true);
    }

    public static CompletableFuture<?>
    setServerResourcePack(File file) {
        return getMinecraft().getClientPackSource().setServerPack(
                file
                , PackSource.SERVER
        );
    }

    public static <T> void addCallback(
            CompletableFuture<T> future,
            Consumer<T> success,
            Consumer<Throwable> failure
    ) {
        future.thenAccept(success).exceptionally(throwable -> {
            failure.accept(throwable);
            return null;
        });
    }

    public static List<VertexFormatElement> getElements(VertexFormat vertexFormat) {
        return vertexFormat.getElements();
    }


    public static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public static void addButton(Screen screen, Button button) 
    {
    	//TODO
        //GuiScreenAccessor acc = (GuiScreenAccessor) screen;
        //acc.invokeAddButton(button);
    	
    	List<Widget> renderables = ObfuscationReflectionHelper.getPrivateValue(Screen.class, screen, "f_169369_");
    	List<GuiEventListener> children = ObfuscationReflectionHelper.getPrivateValue(Screen.class, screen, "f_96540_");
    	List<NarratableEntry> narratables = ObfuscationReflectionHelper.getPrivateValue(Screen.class, screen, "f_169368_");
    	renderables.add(button);
    	children.add(button);
    	narratables.add(button);
    }	

    public static Optional<AbstractWidget> findButton(Collection<AbstractWidget> buttonList, String text, int id) {
        final MutableComponent message = Component.translatable(text);
        for (AbstractWidget b : buttonList) {
            if (message.equals(b.getMessage())) {
                return Optional.of(b);
            }
            // Fuzzy match (copy does not include children)
            if (b.getMessage() != null && b.getMessage().plainCopy().equals(message)) {
                return Optional.of(b);
            }
        }
        return Optional.empty();
    }

    public static void processKeyBinds() {
        ((MinecraftMethodAccessor) getMinecraft()).replayModProcessKeyBinds();
    }


    public static long milliTime() {
        return Util.getMillis();
    }

    // TODO: this can be inlined once https://github.com/SpongePowered/Mixin/issues/305 is fixed
    public static Vec3 getPosition(Particle particle, float partialTicks) {
        ParticleAccessor acc = (ParticleAccessor) particle;
        double x = acc.getPrevPosX() + (acc.getPosX() - acc.getPrevPosX()) * partialTicks;
        double y = acc.getPrevPosY() + (acc.getPosY() - acc.getPrevPosY()) * partialTicks;
        double z = acc.getPrevPosZ() + (acc.getPosZ() - acc.getPrevPosZ()) * partialTicks;
        return new Vec3(x, y, z);
    }

    public static void openFile(File file) {
        Util.getPlatform().openFile(file);
    }

    public static void openURL(URI url) {
        Util.getPlatform().openUri(url);
    }


    private static Boolean hasOptifine;

    public static boolean hasOptifine() {
        if (hasOptifine == null) {
            try {
                Class.forName("Config");
                hasOptifine = true;
            } catch (ClassNotFoundException e) {
                hasOptifine = false;
            }
        }
        return hasOptifine;
    }


    public static abstract class Keyboard {
        public static final int KEY_LCONTROL = GLFW.GLFW_KEY_LEFT_CONTROL;
        public static final int KEY_LSHIFT = GLFW.GLFW_KEY_LEFT_SHIFT;
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
        public static final int KEY_F1 = GLFW.GLFW_KEY_F1;
        public static final int KEY_A = GLFW.GLFW_KEY_A;
        public static final int KEY_B = GLFW.GLFW_KEY_B;
        public static final int KEY_C = GLFW.GLFW_KEY_C;
        public static final int KEY_D = GLFW.GLFW_KEY_D;
        public static final int KEY_E = GLFW.GLFW_KEY_E;
        public static final int KEY_F = GLFW.GLFW_KEY_F;
        public static final int KEY_G = GLFW.GLFW_KEY_G;
        public static final int KEY_H = GLFW.GLFW_KEY_H;
        public static final int KEY_I = GLFW.GLFW_KEY_I;
        public static final int KEY_J = GLFW.GLFW_KEY_J;
        public static final int KEY_K = GLFW.GLFW_KEY_K;
        public static final int KEY_L = GLFW.GLFW_KEY_L;
        public static final int KEY_M = GLFW.GLFW_KEY_M;
        public static final int KEY_N = GLFW.GLFW_KEY_N;
        public static final int KEY_O = GLFW.GLFW_KEY_O;
        public static final int KEY_P = GLFW.GLFW_KEY_P;
        public static final int KEY_Q = GLFW.GLFW_KEY_Q;
        public static final int KEY_R = GLFW.GLFW_KEY_R;
        public static final int KEY_S = GLFW.GLFW_KEY_S;
        public static final int KEY_T = GLFW.GLFW_KEY_T;
        public static final int KEY_U = GLFW.GLFW_KEY_U;
        public static final int KEY_V = GLFW.GLFW_KEY_V;
        public static final int KEY_W = GLFW.GLFW_KEY_W;
        public static final int KEY_X = GLFW.GLFW_KEY_X;
        public static final int KEY_Y = GLFW.GLFW_KEY_Y;
        public static final int KEY_Z = GLFW.GLFW_KEY_Z;

        public static boolean hasControlDown() {
            return Screen.hasControlDown();
        }

        public static boolean isKeyDown(int keyCode) {
            return InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), keyCode);
        }
    }
}

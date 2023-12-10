package com.replaymod.render.gui.progress;

import java.io.Closeable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.replaymod.render.hooks.MinecraftClientExt;
import com.replaymod.replay.mixin.MainWindowAccessor;

import net.minecraft.client.Minecraft;

public class VirtualWindow implements Closeable {
	private final Minecraft mc;
	private final Window window;
	private final MainWindowAccessor acc;
	private final RenderTarget guiFramebuffer;
	private boolean isBound;
	private int framebufferWidth;
	private int framebufferHeight;
	private int gameWidth;
	private int gameHeight;

	public VirtualWindow(Minecraft mc) {
		this.mc = mc;
		this.window = mc.getWindow();
		this.acc = (MainWindowAccessor) (Object) this.window;
		this.framebufferWidth = this.acc.getFramebufferWidth();
		this.framebufferHeight = this.acc.getFramebufferHeight();
		this.guiFramebuffer = new TextureTarget(this.framebufferWidth, this.framebufferHeight, true, false);
		MinecraftClientExt.get(mc).setWindowDelegate(this);
	}

	public void close() {
		this.guiFramebuffer.destroyBuffers();
		MinecraftClientExt.get(this.mc).setWindowDelegate((VirtualWindow) null);
	}

	public void bind() {
		this.gameWidth = this.acc.getFramebufferWidth();
		this.gameHeight = this.acc.getFramebufferHeight();
		this.acc.setFramebufferWidth(this.framebufferWidth);
		this.acc.setFramebufferHeight(this.framebufferHeight);
		this.applyScaleFactor();
		this.isBound = true;
	}

	public void unbind() {
		this.acc.setFramebufferWidth(this.gameWidth);
		this.acc.setFramebufferHeight(this.gameHeight);
		this.applyScaleFactor();
		this.isBound = false;
	}

	public void beginWrite() {
		this.guiFramebuffer.bindWrite(true);
	}

	public void endWrite() {
		this.guiFramebuffer.unbindWrite();
	}

	public void flip() {
		this.guiFramebuffer.blitToScreen(this.framebufferWidth, this.framebufferHeight);
		this.window.updateDisplay();
	}

	public void onResolutionChanged(int newWidth, int newHeight) {
		if (newWidth != 0 && newHeight != 0) {
			if (this.framebufferWidth != newWidth || this.framebufferHeight != newHeight) {
				this.framebufferWidth = newWidth;
				this.framebufferHeight = newHeight;
				this.guiFramebuffer.resize(newWidth, newHeight, false);
				this.applyScaleFactor();
				if (this.mc.screen != null) {
					this.mc.screen.resize(this.mc, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
				}

			}
		}
	}

	private void applyScaleFactor() {
		this.window.setGuiScale((double) this.window
				.calculateScale((Integer) this.mc.options.guiScale().get().intValue(), this.mc.isEnforceUnicode()));
	}

	public int getFramebufferWidth() {
		return this.framebufferWidth;
	}

	public int getFramebufferHeight() {
		return this.framebufferHeight;
	}

	public boolean isBound() {
		return this.isBound;
	}
}

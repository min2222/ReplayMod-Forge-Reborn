package com.replaymod.replay;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

public class NoGuiScreenshot {
	private final Image image;
	private final int width;
	private final int height;

	private NoGuiScreenshot(Image image, int width, int height) {
		this.image = image;
		this.width = width;
		this.height = height;
	}

	public Image getImage() {
		return this.image;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public static ListenableFuture<NoGuiScreenshot> take(Minecraft mc, int width, int height) {
		final SettableFuture<NoGuiScreenshot> future = SettableFuture.create();
		Runnable runnable = new Runnable() {
			public void run() {
				if (!future.isCancelled()) {
					int frameWidth = mc.getWindow().getWidth();
					int frameHeight = mc.getWindow().getHeight();
					boolean guiHidden = mc.options.hideGui;

					label54: {
						try {
							mc.options.hideGui = true;
							MCVer.pushMatrix();
							RenderSystem.clear(16640, true);
							mc.getMainRenderTarget().bindWrite(true);
							RenderSystem.enableTexture();
							float tickDelta = mc.getPartialTick();
							mc.gameRenderer.renderLevel(tickDelta, System.nanoTime(), new PoseStack());
							mc.getMainRenderTarget().unbindWrite();
							MCVer.popMatrix();
							MCVer.pushMatrix();
							mc.getMainRenderTarget().blitToScreen(frameWidth, frameHeight);
							MCVer.popMatrix();
							break label54;
						} catch (Throwable var16) {
							future.setException(var16);
						} finally {
							mc.options.hideGui = guiHidden;
						}

						return;
					}

					try {
						Image image = new Image(Screenshot.takeScreenshot(mc.getMainRenderTarget()));
						int imageWidth = image.getWidth();
						int imageHeight = image.getHeight();
						float scaleFactor = Math.max((float) width / (float) imageWidth,
								(float) height / (float) imageHeight);
						int croppedWidth = Math.min(Math.max(0, (int) ((float) width / scaleFactor)), imageWidth);
						int croppedHeight = Math.min(Math.max(0, (int) ((float) height / scaleFactor)), imageHeight);
						int offsetX = (imageWidth - croppedWidth) / 2;
						int offsetY = (imageHeight - croppedHeight) / 2;
						image = image.scaledSubRect(offsetX, offsetY, croppedWidth, croppedHeight, width, height);
						future.set(new NoGuiScreenshot(image, width, height));
					} catch (Throwable var15) {
						future.setException(var15);
					}

				}
			}
		};
		ReplayMod.instance.runLater(runnable);
		return future;
	}
}

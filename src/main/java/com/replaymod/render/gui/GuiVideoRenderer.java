package com.replaymod.render.gui;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.NativeImage;
import com.replaymod.core.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiCheckbox;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiProgressBar;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.render.capturer.RenderInfo;
import com.replaymod.render.rendering.VideoRenderer;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class GuiVideoRenderer extends GuiScreen implements Tickable {
	private static final ResourceLocation NO_PREVIEW_TEXTURE = new ResourceLocation("replaymod", "logo.jpg");
	private final VideoRenderer renderer;
	public final GuiLabel title = (GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.rendering.title",
			new Object[0]);
	public final GuiPanel imagePanel = (GuiPanel) (new GuiPanel() {
		public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
			if (GuiVideoRenderer.this.previewCheckbox.isChecked()) {
				GuiVideoRenderer.this.renderPreview(renderer, size);
			} else {
				GuiVideoRenderer.this.renderNoPreview(renderer, size);
			}

		}
	}).setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
	public final GuiCheckbox previewCheckbox = (GuiCheckbox) (new GuiCheckbox())
			.setI18nLabel("replaymod.gui.rendering.preview", new Object[0]);
	public final GuiLabel renderTime = new GuiLabel();
	public final GuiLabel remainingTime = new GuiLabel();
	public final GuiProgressBar progressBar = new GuiProgressBar();
	public final GuiPanel buttonPanel = (GuiPanel) (new GuiPanel()).setLayout((new HorizontalLayout()).setSpacing(4));
	public final GuiButton pauseButton;
	public final GuiButton cancelButton;
	private DynamicTexture previewTexture;
	private boolean previewTextureDirty;
	private int renderTimeTaken;
	private long prevTime;
	private long frameStartTime;
	private int prevRenderedFrames;
	private int renderTimeLeft;
	private int[] renderTimes;
	private int currentIndex;

	public GuiVideoRenderer(VideoRenderer renderer) {
		this.pauseButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton(this.buttonPanel))
				.onClick(new Runnable() {
					public void run() {
						if (GuiVideoRenderer.this.renderer.isPaused()) {
							GuiVideoRenderer.this.pauseButton.setI18nLabel("replaymod.gui.rendering.pause",
									new Object[0]);
							GuiVideoRenderer.this.renderer.setPaused(false);
						} else {
							GuiVideoRenderer.this.pauseButton.setI18nLabel("replaymod.gui.rendering.resume",
									new Object[0]);
							GuiVideoRenderer.this.renderer.setPaused(true);
						}

					}
				})).setI18nLabel("replaymod.gui.rendering.pause", new Object[0])).setSize(150, 20);
		this.cancelButton = (GuiButton) ((GuiButton) (new GuiButton(this.buttonPanel) {
			boolean waitingForConfirmation;

			public boolean mouseClick(ReadablePoint position, int button) {
				boolean result = super.mouseClick(position, button);
				if (this.waitingForConfirmation && !result) {
					this.setI18nLabel("replaymod.gui.rendering.cancel", new Object[0]);
					this.waitingForConfirmation = false;
				}

				return result;
			}

			public void onClick() {
				super.onClick();
				if (!this.waitingForConfirmation) {
					this.setI18nLabel("replaymod.gui.rendering.cancel.callback", new Object[0]);
					this.waitingForConfirmation = true;
				} else {
					GuiVideoRenderer.this.renderer.cancel();
				}

			}
		}).setI18nLabel("replaymod.gui.rendering.cancel", new Object[0])).setSize(150, 20);
		final GuiPanel contentPanel = (GuiPanel) ((GuiPanel) (new GuiPanel(this))
				.setLayout(new CustomLayout<GuiPanel>() {
					protected void layout(GuiPanel container, int width, int height) {
						this.size(GuiVideoRenderer.this.progressBar, width, 20);
						this.pos(GuiVideoRenderer.this.title, width / 2 - this.width(GuiVideoRenderer.this.title) / 2,
								0);
						this.pos(GuiVideoRenderer.this.imagePanel, 0,
								this.y(GuiVideoRenderer.this.title) + this.height(GuiVideoRenderer.this.title) + 5);
						this.pos(GuiVideoRenderer.this.buttonPanel,
								width / 2 - this.width(GuiVideoRenderer.this.buttonPanel) / 2,
								height - this.height(GuiVideoRenderer.this.buttonPanel));
						this.pos(GuiVideoRenderer.this.progressBar,
								width / 2 - this.width(GuiVideoRenderer.this.progressBar) / 2,
								this.y(GuiVideoRenderer.this.buttonPanel) - 5
										- this.height(GuiVideoRenderer.this.progressBar));
						this.pos(GuiVideoRenderer.this.renderTime, 0, this.y(GuiVideoRenderer.this.progressBar) - 2
								- this.height(GuiVideoRenderer.this.renderTime));
						this.pos(GuiVideoRenderer.this.remainingTime,
								width - this.width(GuiVideoRenderer.this.remainingTime),
								this.y(GuiVideoRenderer.this.progressBar) - 2
										- this.height(GuiVideoRenderer.this.renderTime));
						this.pos(GuiVideoRenderer.this.previewCheckbox,
								width / 2 - this.width(GuiVideoRenderer.this.previewCheckbox) / 2,
								this.y(GuiVideoRenderer.this.renderTime) - 10
										- this.height(GuiVideoRenderer.this.previewCheckbox));
						this.size(GuiVideoRenderer.this.imagePanel, width, this.y(GuiVideoRenderer.this.previewCheckbox)
								- 5 - this.y(GuiVideoRenderer.this.imagePanel));
					}
				})).addElements((LayoutData) null, new GuiElement[] { this.title, this.imagePanel, this.previewCheckbox,
						this.renderTime, this.remainingTime, this.progressBar, this.buttonPanel });
		this.setLayout(new CustomLayout<GuiScreen>() {
			protected void layout(GuiScreen container, int width, int height) {
				this.pos(contentPanel, 5, 3);
				this.size(contentPanel, width - 10, height - 10);
			}
		});
		this.setBackground(AbstractGuiScreen.Background.DIRT);
		this.renderTimeTaken = 0;
		this.prevTime = -1L;
		this.frameStartTime = -1L;
		this.prevRenderedFrames = 0;
		this.renderTimeLeft = 0;
		this.renderTimes = new int[50];
		this.currentIndex = 0;
		this.renderer = renderer;
	}

	public void tick() {
		long current = System.nanoTime() / 1000000L;
		if (!this.renderer.isPaused() && this.renderer.getFramesDone() > 0 && this.prevTime > -1L) {
			this.renderTimeTaken = (int) ((long) this.renderTimeTaken + (current - this.prevTime));
		} else {
			this.frameStartTime = current;
		}

		this.prevTime = current;
		int framesRendered;
		int renderTime;
		if (this.prevRenderedFrames < this.renderer.getFramesDone()) {
			if (this.prevRenderedFrames > 0) {
				framesRendered = this.renderer.getFramesDone() - this.prevRenderedFrames;
				renderTime = (int) (current - this.frameStartTime);
				int avgRenderTime = renderTime / framesRendered;

				int validValues;
				for (validValues = 0; validValues < framesRendered; ++validValues) {
					this.renderTimes[this.currentIndex] = avgRenderTime;
					++this.currentIndex;
					if (this.currentIndex >= this.renderTimes.length) {
						this.currentIndex = 0;
					}
				}

				validValues = 0;
				int totalTime = 0;
				int[] var8 = this.renderTimes;
				int var9 = var8.length;

				for (int var10 = 0; var10 < var9; ++var10) {
					int i = var8[var10];
					if (i > 0) {
						totalTime += i;
						++validValues;
					}
				}

				float averageRenderTime = validValues > 0 ? (float) (totalTime / validValues) : 0.0F;
				this.renderTimeLeft = Math.round(averageRenderTime
						* (float) (this.renderer.getTotalFrames() - this.renderer.getFramesDone()) / 1000.0F);
			}

			this.frameStartTime = current;
			this.prevRenderedFrames = this.renderer.getFramesDone();
		}

		GuiLabel var10000 = this.renderTime;
		String var10001 = I18n.get("replaymod.gui.rendering.timetaken", new Object[0]);
		var10000.setText(var10001 + ": " + this.secToString(this.renderTimeTaken / 1000));
		var10000 = this.remainingTime;
		var10001 = I18n.get("replaymod.gui.rendering.timeleft", new Object[0]);
		var10000.setText(var10001 + ": " + this.secToString(this.renderTimeLeft));
		framesRendered = this.renderer.getFramesDone();
		renderTime = this.renderer.getTotalFrames();
		this.progressBar.setI18nLabel("replaymod.gui.rendering.progress", new Object[] { framesRendered, renderTime });
		this.progressBar.setProgress((float) framesRendered / (float) renderTime);
	}

	private String secToString(int seconds) {
		int hours = seconds / 3600;
		int min = seconds / 60 - hours * 60;
		int sec = seconds - (min * 60 + hours * 60 * 60);
		StringBuilder builder = new StringBuilder();
		if (hours > 0) {
			builder.append(hours).append(I18n.get("replaymod.gui.hours", new Object[0]));
		}

		if (min > 0 || hours > 0) {
			builder.append(min).append(I18n.get("replaymod.gui.minutes", new Object[0]));
		}

		builder.append(sec).append(I18n.get("replaymod.gui.seconds", new Object[0]));
		return builder.toString();
	}

	private synchronized void renderPreview(GuiRenderer guiRenderer, ReadableDimension size) {
		ReadableDimension videoSize = this.renderer.getFrameSize();
		int videoWidth = videoSize.getWidth();
		int videoHeight = videoSize.getHeight();
		if (this.previewTexture == null) {
			this.previewTexture = new DynamicTexture(videoWidth, videoHeight, true);
		}

		if (this.previewTextureDirty) {
			this.previewTexture.upload();
			this.previewTextureDirty = false;
		}

		guiRenderer.bindTexture(this.previewTexture.getId());
		this.renderPreviewTexture(guiRenderer, size, videoWidth, videoHeight);
	}

	private void renderNoPreview(GuiRenderer guiRenderer, ReadableDimension size) {
		guiRenderer.bindTexture(NO_PREVIEW_TEXTURE);
		this.renderPreviewTexture(guiRenderer, size, 1280, 720);
	}

	private void renderPreviewTexture(GuiRenderer guiRenderer, ReadableDimension size, int videoWidth,
			int videoHeight) {
		Dimension dimension = Utils.fitIntoBounds(new Dimension(videoWidth, videoHeight), size);
		int width = dimension.getWidth();
		int height = dimension.getHeight();
		int x = (size.getWidth() - width) / 2;
		int y = (size.getHeight() - height) / 2;
		guiRenderer.drawTexturedRect(x, y, 0, 0, width, height, videoWidth, videoHeight, videoWidth, videoHeight);
	}

	public void updatePreview(ByteBuffer buffer, ReadableDimension size) {
		if (this.previewCheckbox.isChecked() && this.previewTexture != null) {
			buffer.mark();
			synchronized (this) {
				NativeImage data = this.previewTexture.getPixels();

				assert data != null;

				int width = size.getWidth();
				int y = 0;

				while (true) {
					if (y >= size.getHeight()) {
						this.previewTextureDirty = true;
						break;
					}

					for (int x = 0; x < width; ++x) {
						int b = buffer.get() & 255;
						int g = buffer.get() & 255;
						int r = buffer.get() & 255;
						buffer.get();
						int value = -16777216 | b << 16 | g << 8 | r;
						data.setPixelRGBA(x, y, value);
					}

					++y;
				}
			}

			buffer.reset();
		}

	}
}

package com.replaymod.extras.youtube;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.IOUtils;

import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.extras.ReplayModExtras;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiImage;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiProgressBar;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiTextArea;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiFileChooserPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import com.replaymod.render.RenderSettings;

import joptsimple.internal.Strings;
import net.minecraft.client.resources.language.I18n;

public class GuiYoutubeUpload extends GuiScreen {
	private final GuiScreen previousScreen;
	private final File videoFile;
	private final int videoFrames;
	private final RenderSettings settings;
	private String thumbnailFormat;
	private byte[] thumbnailImage;
	public final Runnable inputValidation = new Runnable() {
		public void run() {
			String problem = null;
			if (GuiYoutubeUpload.this.nameField.getText().isEmpty()) {
				problem = "replaymod.gui.titleempty";
			}

			if (GuiYoutubeUpload.this.thumbnailImage != null) {
				if (GuiYoutubeUpload.this.thumbnailImage.length > 2097152) {
					problem = "replaymod.gui.videothumbnailtoolarge";
				}

				if (!Arrays.asList("jpeg", "png").contains(GuiYoutubeUpload.this.thumbnailFormat)) {
					problem = "replaymod.gui.videothumbnailformat";
				}
			}

			if (GuiYoutubeUpload.this.upload == null) {
				if (problem == null) {
					GuiYoutubeUpload.this.uploadButton.setEnabled();
					GuiYoutubeUpload.this.uploadButton.setTooltip((GuiElement) null);
				} else {
					GuiYoutubeUpload.this.uploadButton.setDisabled();
					GuiYoutubeUpload.this.uploadButton
							.setTooltip((new GuiTooltip()).setI18nText(problem, new Object[0]));
				}
			}

		}
	};
	public final GuiTextField nameField = (GuiTextField) ((GuiTextField) (new GuiTextField())
			.setI18nHint("replaymod.gui.videotitle", new Object[0])).onTextChanged((s) -> {
				this.inputValidation.run();
			});
	public final GuiTextArea descriptionField = (GuiTextArea) ((GuiTextArea) ((GuiTextArea) (new GuiTextArea())
			.setMaxCharCount(Integer.MAX_VALUE)).setMaxTextWidth(Integer.MAX_VALUE))
			.setMaxTextHeight(Integer.MAX_VALUE);
	public final GuiTextField tagsField;
	public final GuiProgressBar progressBar;
	public final GuiPanel leftPanel;
	public final GuiDropdownMenu<VideoVisibility> visibilityDropdown;
	public final GuiButton thumbnailButton;
	public final GuiImage thumbnail;
	public final GuiButton uploadButton;
	public final GuiButton closeButton;
	public final GuiPanel rightPanel;
	private YoutubeUploader upload;

	public GuiYoutubeUpload(GuiScreen previousScreen, File videoFile, int videoFrames, RenderSettings settings) {
		this.descriptionField.setText(new String[] { I18n.get("replaymod.gui.videodescription", new Object[0]) });
		this.tagsField = (GuiTextField) (new GuiTextField()).setI18nHint("replaymod.gui.videotags", new Object[0]);
		((GuiTextField) this.nameField.setNext(this.descriptionField)).getNext().setNext(this.tagsField).getNext()
				.setNext(this.nameField);
		this.progressBar = new GuiProgressBar();
		this.leftPanel = (GuiPanel) ((GuiPanel) (new GuiPanel(this)).setLayout(new CustomLayout<GuiPanel>() {
			protected void layout(GuiPanel container, int width, int height) {
				this.size(GuiYoutubeUpload.this.nameField, width, 20);
				this.size(GuiYoutubeUpload.this.descriptionField, width, height - 90);
				this.size(GuiYoutubeUpload.this.tagsField, width, 20);
				this.size(GuiYoutubeUpload.this.progressBar, width, 20);
				this.pos(GuiYoutubeUpload.this.nameField, 0, 0);
				this.pos(GuiYoutubeUpload.this.descriptionField, 0, 30);
				this.pos(GuiYoutubeUpload.this.tagsField, 0, height - 50);
				this.pos(GuiYoutubeUpload.this.progressBar, 0, height - 20);
			}
		})).addElements((LayoutData) null,
				new GuiElement[] { this.nameField, this.descriptionField, this.tagsField, this.progressBar });
		this.visibilityDropdown = (GuiDropdownMenu) ((GuiDropdownMenu) ((GuiDropdownMenu) (new GuiDropdownMenu())
				.setSize(200, 20)).setValues(VideoVisibility.values())).setSelected(VideoVisibility.PUBLIC);
		this.thumbnailButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).onClick(new Runnable() {
			public void run() {
				GuiFileChooserPopup
						.openLoadGui(GuiYoutubeUpload.this, "replaymod.gui.load", ImageIO.getReaderFileSuffixes())
						.onAccept((file) -> {
							GuiYoutubeUpload.this.thumbnailButton.setLabel(file.getName());

							Image image;
							try {
								GuiYoutubeUpload.this.thumbnailImage = IOUtils.toByteArray(new FileInputStream(file));
								ImageInputStream in = ImageIO.createImageInputStream(
										new ByteArrayInputStream(GuiYoutubeUpload.this.thumbnailImage));
								ImageReader reader = (ImageReader) ImageIO.getImageReaders(in).next();
								GuiYoutubeUpload.this.thumbnailFormat = reader.getFormatName().toLowerCase();
								image = Image.read(
										(InputStream) (new ByteArrayInputStream(GuiYoutubeUpload.this.thumbnailImage)));
							} catch (Throwable var5) {
								var5.printStackTrace();
								GuiYoutubeUpload.this.thumbnailImage = null;
								image = null;
							}

							GuiYoutubeUpload.this.thumbnail.setTexture(image);
							GuiYoutubeUpload.this.inputValidation.run();
						});
			}
		})).setSize(200, 20)).setI18nLabel("replaymod.gui.videothumbnail", new Object[0]);
		this.thumbnail = (GuiImage) ((GuiImage) (new GuiImage()).setSize(200, 112)).setTexture(Utils.DEFAULT_THUMBNAIL);
		this.uploadButton = (GuiButton) (new GuiButton(this)).setSize(98, 20);
		this.closeButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton(this)).onClick(new Runnable() {
			public void run() {
				GuiYoutubeUpload.this.previousScreen.display();
			}
		})).setSize(98, 20)).setI18nLabel("replaymod.gui.back", new Object[0]);
		this.rightPanel = (GuiPanel) ((GuiPanel) (new GuiPanel(this)).addElements((LayoutData) null,
				new GuiElement[] { this.visibilityDropdown, this.thumbnailButton, this.thumbnail }))
				.setLayout((new VerticalLayout(VerticalLayout.Alignment.TOP)).setSpacing(10));
		this.setLayout(new CustomLayout<GuiScreen>() {
			protected void layout(GuiScreen container, int width, int height) {
				this.pos(GuiYoutubeUpload.this.leftPanel, 10, 10);
				this.size(GuiYoutubeUpload.this.leftPanel, width - 200 - 30, height - 20);
				this.pos(GuiYoutubeUpload.this.rightPanel, width - 210, 10);
				this.pos(GuiYoutubeUpload.this.uploadButton, width - 210, height - 30);
				this.pos(GuiYoutubeUpload.this.closeButton, width - 108, height - 30);
			}
		});
		this.setState(false);
		this.inputValidation.run();
		this.previousScreen = previousScreen;
		this.videoFile = videoFile;
		this.videoFrames = videoFrames;
		this.settings = settings;
	}

	private void setState(boolean uploading) {
		this.invokeAll(GuiElement.class, (e) -> {
			e.setEnabled(!uploading);
		});
		this.uploadButton.setEnabled();
		if (uploading) {
			((GuiButton) this.uploadButton.onClick(() -> {
				this.setState(false);
				(new Thread(() -> {
					try {
						this.upload.cancel();
					} catch (InterruptedException var2) {
						var2.printStackTrace();
					}

				})).start();
			})).setI18nLabel("replaymod.gui.cancel", new Object[0]);
		} else {
			((GuiButton) this.uploadButton.onClick(() -> {
				try {
					this.setState(true);
					VideoVisibility visibility = (VideoVisibility) this.visibilityDropdown.getSelectedValue();
					VideoSnippet snippet = new VideoSnippet();
					snippet.setTitle(this.nameField.getText());
					snippet.setDescription(Strings.join(this.descriptionField.getText(), "\n"));
					snippet.setTags(Arrays.asList(this.tagsField.getText().split(",")));
					this.upload = new YoutubeUploader(this.getMinecraft(), this.videoFile, this.videoFrames,
							this.thumbnailFormat, this.thumbnailImage, this.settings, visibility, snippet);
					ListenableFuture<Video> future = this.upload.upload();
					Futures.addCallback(future, new FutureCallback<Video>() {
						public void onSuccess(Video result) {
							String url = "https://youtu.be/" + result.getId();

							try {
								MCVer.openURL((new URL(url)).toURI());
							} catch (Throwable var4) {
								ReplayModExtras.LOGGER.error("Failed to open video URL \"{}\":", url, var4);
							}

							GuiYoutubeUpload.this.upload = null;
							GuiYoutubeUpload.this.progressBar
									.setLabel(I18n.get("replaymod.gui.ytuploadprogress.done", new Object[] { url }));
							GuiYoutubeUpload.this.setState(false);
						}

						public void onFailure(Throwable t) {
							if (!(t instanceof InterruptedException) && !GuiYoutubeUpload.this.upload.isCancelled()) {
								t.printStackTrace();
								GuiYoutubeUpload.this.progressBar.setLabel(t.getLocalizedMessage());
							} else {
								GuiYoutubeUpload.this.progressBar.setProgress(0.0F);
								GuiYoutubeUpload.this.progressBar.setLabel("%d%%");
							}

							GuiYoutubeUpload.this.upload = null;
							GuiYoutubeUpload.this.setState(false);
						}
					}, Runnable::run);
				} catch (IOException | GeneralSecurityException var4) {
					var4.printStackTrace();
				}

			})).setI18nLabel("replaymod.gui.upload", new Object[0]);
		}

	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		if (this.upload != null && this.upload.getState() != null) {
			this.progressBar.setProgress((float) this.upload.getProgress());
			this.progressBar.setI18nLabel(
					"replaymod.gui.ytuploadprogress." + this.upload.getState().name().toLowerCase(), new Object[0]);
		}

		super.draw(renderer, size, renderInfo);
	}
}

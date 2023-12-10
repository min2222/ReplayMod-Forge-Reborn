package com.replaymod.render.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiCheckbox;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiNumberField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiSlider;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiCheckbox;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiColorPicker;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiFileChooserPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.FFmpegWriter;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.ReplayModRender;
import com.replaymod.render.rendering.VideoRenderer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.path.Timeline;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class GuiRenderSettings extends AbstractGuiPopup<GuiRenderSettings> {
	public final GuiPanel contentPanel;
	public final GuiVerticalList settingsList;
	public final GuiDropdownMenu<RenderSettings.RenderMethod> renderMethodDropdown;
	public final GuiDropdownMenu<RenderSettings.EncodingPreset> encodingPresetDropdown;
	public final GuiNumberField videoWidth;
	public final GuiNumberField videoHeight;
	public final GuiSlider frameRateSlider;
	public final GuiPanel videoResolutionPanel;
	public final GuiNumberField bitRateField;
	public final GuiDropdownMenu<String> bitRateUnit;
	public final GuiButton outputFileButton;
	public final GuiPanel mainPanel;
	public final GuiCheckbox nametagCheckbox;
	public final GuiCheckbox alphaCheckbox;
	public final GuiPanel stabilizePanel;
	public final GuiCheckbox stabilizeYaw;
	public final GuiCheckbox stabilizePitch;
	public final GuiCheckbox stabilizeRoll;
	public final GuiCheckbox chromaKeyingCheckbox;
	public final GuiColorPicker chromaKeyingColor;
	public static final int MIN_SPHERICAL_FOV = 120;
	public static final int MAX_SPHERICAL_FOV = 360;
	public static final int SPHERICAL_FOV_STEP_SIZE = 5;
	public final GuiSlider sphericalFovSlider;
	public final GuiCheckbox injectSphericalMetadata;
	public final GuiCheckbox depthMap;
	public final GuiCheckbox cameraPathExport;
	public final GuiDropdownMenu<RenderSettings.AntiAliasing> antiAliasingDropdown;
	public final GuiPanel advancedPanel;
	public final GuiTextField exportCommand;
	public final GuiTextField exportArguments;
	public final GuiButton exportReset;
	public final GuiPanel commandlinePanel;
	public final GuiPanel buttonPanel;
	public final GuiButton queueButton;
	public final GuiButton renderButton;
	public final GuiButton cancelButton;
	private final AbstractGuiScreen<?> screen;
	private final ReplayHandler replayHandler;
	private final Timeline timeline;
	private File outputFile;
	private boolean userDefinedOutputFileName;

	public GuiRenderSettings(AbstractGuiScreen<?> container, ReplayHandler replayHandler, Timeline timeline) {
		super(container);
		this.disablePopupBackground();
		this.contentPanel = (GuiPanel) (new GuiPanel(this.popup)).setBackgroundColor(new Color(0, 0, 0, 230));
		this.settingsList = (GuiVerticalList) (new GuiVerticalList(this.contentPanel)).setDrawSlider(true);
		this.renderMethodDropdown = (GuiDropdownMenu) ((GuiDropdownMenu) ((GuiDropdownMenu) (new GuiDropdownMenu())
				.onSelection(new Consumer<Integer>() {
					public void consume(Integer old) {
						if (GuiRenderSettings.this.renderMethodDropdown
								.getSelectedValue() == RenderSettings.RenderMethod.BLEND
								^ GuiRenderSettings.this.encodingPresetDropdown
										.getSelectedValue() == RenderSettings.EncodingPreset.BLEND) {
							if (GuiRenderSettings.this.renderMethodDropdown
									.getSelectedValue() == RenderSettings.RenderMethod.BLEND) {
								GuiRenderSettings.this.encodingPresetDropdown
										.setSelected(RenderSettings.EncodingPreset.BLEND);
							} else {
								GuiRenderSettings.this.encodingPresetDropdown
										.setSelected(RenderSettings.EncodingPreset.MP4_CUSTOM);
							}
						}

						GuiRenderSettings.this.updateInputs();
					}
				})).setMinSize(new Dimension(0, 20))).setValues(RenderSettings.RenderMethod.getSupported());
		Iterator var4 = this.renderMethodDropdown.getDropdownEntries().entrySet().iterator();

		while (var4.hasNext()) {
			Entry<RenderSettings.RenderMethod, IGuiClickable> entry = (Entry) var4.next();
			((IGuiClickable) entry.getValue()).setTooltip(
					(new GuiTooltip()).setText(((RenderSettings.RenderMethod) entry.getKey()).getDescription()));
		}

		this.encodingPresetDropdown = (GuiDropdownMenu) ((GuiDropdownMenu) ((GuiDropdownMenu) (new GuiDropdownMenu())
				.onSelection(new Consumer<Integer>() {
					public void consume(Integer old) {
						RenderSettings.EncodingPreset newPreset = (RenderSettings.EncodingPreset) GuiRenderSettings.this.encodingPresetDropdown
								.getSelectedValue();
						if (newPreset == RenderSettings.EncodingPreset.BLEND
								&& GuiRenderSettings.this.encodingPresetDropdown
										.getSelectedValue() == RenderSettings.EncodingPreset.BLEND) {
							GuiRenderSettings.this.renderMethodDropdown.setSelected(RenderSettings.RenderMethod.BLEND);
						}

						GuiRenderSettings.this.exportArguments.setText(newPreset.getValue());
						if (GuiRenderSettings.this.outputFile != null) {
							GuiRenderSettings.this.outputFile = GuiRenderSettings.this
									.conformExtension(GuiRenderSettings.this.outputFile, newPreset);
							GuiRenderSettings.this.outputFileButton
									.setLabel(GuiRenderSettings.this.outputFile.getName());
						}

						GuiRenderSettings.this.updateInputs();
					}
				})).setMinSize(new Dimension(0, 20))).setValues(RenderSettings.EncodingPreset.getSupported());
		this.videoWidth = (GuiNumberField) ((GuiNumberField) ((GuiNumberField) (new GuiNumberField()).setSize(50, 20))
				.setMinValue(1)).setValidateOnFocusChange(true);
		this.videoHeight = (GuiNumberField) ((GuiNumberField) ((GuiNumberField) (new GuiNumberField()).setSize(50, 20))
				.setMinValue(1)).setValidateOnFocusChange(true);
		this.frameRateSlider = (GuiSlider) ((GuiSlider) ((GuiSlider) (new GuiSlider()).onValueChanged(new Runnable() {
			public void run() {
				GuiSlider var10000 = GuiRenderSettings.this.frameRateSlider;
				String var10001 = I18n.get("replaymod.gui.rendersettings.framerate", new Object[0]);
				var10000.setText(var10001 + ": " + (GuiRenderSettings.this.frameRateSlider.getValue() + 10));
			}
		})).setSize(122, 20)).setSteps(110);
		this.videoResolutionPanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new HorizontalLayout(HorizontalLayout.Alignment.RIGHT)).setSpacing(2)))
				.addElements(new HorizontalLayout.Data(0.5D),
						new GuiElement[] { this.videoWidth, (new GuiLabel()).setText("*"), this.videoHeight });
		this.bitRateField = (GuiNumberField) ((GuiNumberField) ((GuiNumberField) (new GuiNumberField()).setValue(10))
				.setSize(50, 20)).setValidateOnFocusChange(true);
		this.bitRateUnit = (GuiDropdownMenu) ((GuiDropdownMenu) ((GuiDropdownMenu) (new GuiDropdownMenu()).setSize(50,
				20)).setValues(new String[] { "bps", "kbps", "mbps" })).setSelected("mbps");
		this.outputFileButton = (GuiButton) ((GuiButton) (new GuiButton()).setMinSize(new Dimension(0, 20)))
				.onClick(new Runnable() {
					public void run() {
						GuiFileChooserPopup popup = GuiFileChooserPopup.openSaveGui(GuiRenderSettings.this,
								"replaymod.gui.save",
								((RenderSettings.EncodingPreset) GuiRenderSettings.this.encodingPresetDropdown
										.getSelectedValue()).getFileExtension());
						popup.setFolder(GuiRenderSettings.getParentFile(GuiRenderSettings.this.outputFile));
						popup.setFileName(GuiRenderSettings.this.outputFile.getName());
						popup.onAccept((file) -> {
							if (!file.getName().equals(GuiRenderSettings.this.outputFile.getName())) {
								GuiRenderSettings.this.userDefinedOutputFileName = true;
							}

							GuiRenderSettings.this.outputFile = file;
							GuiRenderSettings.this.outputFileButton.setLabel(file.getName());
						});
					}
				});
		this.mainPanel = (GuiPanel) ((GuiPanel) (new GuiPanel()).addElements(new GridLayout.Data(1.0D, 0.5D),
				new GuiElement[] { (new GuiLabel()).setI18nText("replaymod.gui.rendersettings.renderer", new Object[0]),
						this.renderMethodDropdown,
						(new GuiLabel()).setI18nText("replaymod.gui.rendersettings.presets", new Object[0]),
						this.encodingPresetDropdown,
						(new GuiLabel()).setI18nText("replaymod.gui.rendersettings.customresolution", new Object[0]),
						this.videoResolutionPanel,
						(new GuiLabel()).setI18nText("replaymod.gui.rendersettings.bitrate", new Object[0]),
						((GuiPanel) (new GuiPanel()).addElements((LayoutData) null,
								new GuiElement[] { ((GuiPanel) (new GuiPanel()).addElements((LayoutData) null,
										new GuiElement[] { this.bitRateField, this.bitRateUnit }))
										.setLayout(new HorizontalLayout()), this.frameRateSlider }))
								.setLayout((new HorizontalLayout(HorizontalLayout.Alignment.RIGHT)).setSpacing(3)),
						(new GuiLabel()).setI18nText("replaymod.gui.rendersettings.outputfile", new Object[0]),
						this.outputFileButton }))
				.setLayout((new GridLayout()).setCellsEqualSize(false).setColumns(2).setSpacingX(5).setSpacingY(5));
		this.nametagCheckbox = (GuiCheckbox) (new GuiCheckbox()).setI18nLabel("replaymod.gui.rendersettings.nametags",
				new Object[0]);
		this.alphaCheckbox = (GuiCheckbox) (new GuiCheckbox()).setI18nLabel("replaymod.gui.rendersettings.includealpha",
				new Object[0]);
		this.stabilizePanel = (GuiPanel) (new GuiPanel()).setLayout((new HorizontalLayout()).setSpacing(10));
		this.stabilizeYaw = (GuiCheckbox) (new GuiCheckbox(this.stabilizePanel)).setI18nLabel("replaymod.gui.yaw",
				new Object[0]);
		this.stabilizePitch = (GuiCheckbox) (new GuiCheckbox(this.stabilizePanel)).setI18nLabel("replaymod.gui.pitch",
				new Object[0]);
		this.stabilizeRoll = (GuiCheckbox) (new GuiCheckbox(this.stabilizePanel)).setI18nLabel("replaymod.gui.roll",
				new Object[0]);
		this.chromaKeyingCheckbox = (GuiCheckbox) (new GuiCheckbox())
				.setI18nLabel("replaymod.gui.rendersettings.chromakey", new Object[0]);
		this.chromaKeyingColor = (GuiColorPicker) (new GuiColorPicker()).setSize(30, 15);
		this.sphericalFovSlider = (GuiSlider) ((GuiSlider) ((GuiSlider) (new GuiSlider())
				.onValueChanged(new Runnable() {
					public void run() {
						GuiSlider var10000 = GuiRenderSettings.this.sphericalFovSlider;
						String var10001 = I18n.get("replaymod.gui.rendersettings.sphericalFov", new Object[0]);
						var10000.setText(var10001 + ": "
								+ (120 + GuiRenderSettings.this.sphericalFovSlider.getValue() * 5) + "°");
						GuiRenderSettings.this.updateInputs();
					}
				})).setSize(200, 20)).setSteps(48);
		this.injectSphericalMetadata = (GuiCheckbox) (new GuiCheckbox())
				.setI18nLabel("replaymod.gui.rendersettings.sphericalmetadata", new Object[0]);
		this.depthMap = (GuiCheckbox) (new GuiCheckbox()).setI18nLabel("replaymod.gui.rendersettings.depthmap",
				new Object[0]);
		this.cameraPathExport = (GuiCheckbox) (new GuiCheckbox())
				.setI18nLabel("replaymod.gui.rendersettings.camerapath", new Object[0]);
		this.antiAliasingDropdown = new GuiDropdownMenu<RenderSettings.AntiAliasing>().setSize(200, 20)
				.setValues(RenderSettings.AntiAliasing.values()).setSelected(RenderSettings.AntiAliasing.NONE);
		this.advancedPanel = (GuiPanel) ((GuiPanel) (new GuiPanel()).setLayout((new VerticalLayout()).setSpacing(15)))
				.addElements((LayoutData) null, new GuiElement[] { this.nametagCheckbox, this.alphaCheckbox,
						((GuiPanel) (new GuiPanel()).setLayout((new GridLayout()).setCellsEqualSize(false).setColumns(2)
								.setSpacingX(5).setSpacingY(15)))
								.addElements(new GridLayout.Data(0.0D, 0.5D),
										new GuiElement[] {
												(new GuiLabel()).setI18nText(
														"replaymod.gui.rendersettings.stabilizecamera", new Object[0]),
												this.stabilizePanel, this.chromaKeyingCheckbox, this.chromaKeyingColor,
												this.injectSphericalMetadata, this.sphericalFovSlider, this.depthMap,
												new GuiLabel(), this.cameraPathExport, new GuiLabel(),
												(new GuiLabel()).setI18nText(
														"replaymod.gui.rendersettings.antialiasing", new Object[0]),
												this.antiAliasingDropdown }) });
		this.exportCommand = (GuiTextField) ((GuiTextField) ((GuiTextField) ((GuiTextField) (new GuiTextField())
				.setI18nHint("replaymod.gui.rendersettings.command", new Object[0])).setSize(55, 20)).setMaxLength(100))
				.onTextChanged((old) -> {
					this.updateInputs();
				});
		this.exportArguments = (GuiTextField) ((GuiTextField) ((GuiTextField) ((GuiTextField) (new GuiTextField())
				.setI18nHint("replaymod.gui.rendersettings.arguments", new Object[0]))
				.setMinSize(new Dimension(245, 20))).setMaxLength(500)).onTextChanged((old) -> {
					this.updateInputs();
				});
		this.exportReset = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton()).setLabel("X")).setSize(20, 20))
				.onClick(() -> {
					this.exportCommand.setText("");
					this.exportArguments
							.setText(((RenderSettings.EncodingPreset) this.encodingPresetDropdown.getSelectedValue())
									.getValue());
					this.updateInputs();
				});
		this.commandlinePanel = (GuiPanel) ((GuiPanel) (new GuiPanel())
				.setLayout((new VerticalLayout()).setSpacing(10)))
				.addElements((LayoutData) null,
						new GuiElement[] {
								((GuiPanel) (new GuiPanel()).setLayout((new HorizontalLayout()).setSpacing(5)))
										.addElements((LayoutData) null,
												new GuiElement[] { this.exportCommand, this.exportArguments,
														this.exportReset }),
								((GuiLabel) (new GuiLabel((new GuiPanel()).setLayout(new CustomLayout<GuiPanel>() {
									protected void layout(GuiPanel container, int width, int height) {
										this.size((GuiElement) container.getChildren().iterator().next(), width,
												height);
									}

									public ReadableDimension calcMinSize(GuiContainer<?> container) {
										return new Dimension(300, 50);
									}
								}))).setI18nText("replaymod.gui.rendersettings.ffmpeg.description", new Object[0]))
										.getContainer() });
		this.buttonPanel = (GuiPanel) (new GuiPanel(this.contentPanel))
				.setLayout((new HorizontalLayout()).setSpacing(4));
		this.queueButton = (GuiButton) ((GuiButton) (new GuiButton(this.buttonPanel)).setSize(100, 20))
				.setI18nLabel("replaymod.gui.rendersettings.addtoqueue", new Object[0]);
		this.renderButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton(this.buttonPanel)).onClick(() -> {
			ReplayMod.instance.runLaterWithoutLock(new Runnable() {
				public void run() {
					GuiRenderSettings.this.close();

					try {
						VideoRenderer videoRenderer = new VideoRenderer(GuiRenderSettings.this.save(false),
								GuiRenderSettings.this.replayHandler, GuiRenderSettings.this.timeline);
						videoRenderer.renderVideo();
					} catch (FFmpegWriter.NoFFmpegException var2) {
						ReplayModRender.LOGGER.error("Rendering video:", var2);
						Minecraft var10000 = GuiRenderSettings.this.getMinecraft();
						AbstractGuiScreen var10003 = GuiRenderSettings.this.getScreen();
						Objects.requireNonNull(var10003);
						var10000.setScreen((new GuiNoFfmpeg(var10003::display)).toMinecraft());
					} catch (FFmpegWriter.FFmpegStartupException var3) {
						GuiExportFailed.tryToRecover(var3, (newSettings) -> {
							GuiRenderSettings.this.exportArguments.setText(newSettings.getExportArguments());
							GuiRenderSettings.this.renderButton.onClick();
						});
					} catch (Throwable var4) {
						Utils.error(ReplayModRender.LOGGER, GuiRenderSettings.this,
								CrashReport.forThrowable(var4, "Rendering video"), () -> {
								});
						GuiRenderSettings.this.getScreen().display();
					}

				}
			});
		})).setSize(100, 20)).setI18nLabel("replaymod.gui.render", new Object[0]);
		this.cancelButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton(this.buttonPanel))
				.onClick(this::close)).setSize(100, 20)).setI18nLabel("replaymod.gui.cancel", new Object[0]);
		com.replaymod.lib.de.johni0702.minecraft.gui.utils.Utils.link(this.videoWidth, this.videoHeight,
				this.bitRateField);
		this.contentPanel.setLayout(new CustomLayout<GuiPanel>() {
			protected void layout(GuiPanel container, int width, int height) {
				this.size(GuiRenderSettings.this.settingsList, width,
						height - this.height(GuiRenderSettings.this.buttonPanel) - 25);
				this.pos(GuiRenderSettings.this.settingsList,
						width / 2 - this.width(GuiRenderSettings.this.settingsList) / 2, 5);
				this.pos(GuiRenderSettings.this.buttonPanel,
						width / 2 - this.width(GuiRenderSettings.this.buttonPanel) / 2,
						this.y(GuiRenderSettings.this.settingsList) + this.height(GuiRenderSettings.this.settingsList)
								+ 10);
			}

			public ReadableDimension calcMinSize(GuiContainer<?> container) {
				ReadableDimension screenSize = GuiRenderSettings.this.getContainer().getMinSize();
				return new Dimension(screenSize.getWidth() - 40, screenSize.getHeight() - 40);
			}
		});
		((GuiPanel) this.settingsList.getListPanel().setLayout((new VerticalLayout()).setSpacing(10))).addElements(
				new VerticalLayout.Data(0.5D),
				new GuiElement[] { (new GuiLabel()).setI18nText("replaymod.gui.rendersettings.video", new Object[0]),
						this.mainPanel, new GuiPanel(),
						(new GuiLabel()).setI18nText("replaymod.gui.rendersettings.advanced", new Object[0]),
						this.advancedPanel, new GuiPanel(),
						(new GuiLabel()).setI18nText("replaymod.gui.rendersettings.commandline", new Object[0]),
						this.commandlinePanel });
		this.videoWidth.onTextChanged(new Consumer<String>() {
			public void consume(String old) {
				GuiRenderSettings.this.updateInputs();
			}
		});
		this.videoHeight.onTextChanged(new Consumer<String>() {
			public void consume(String obj) {
				GuiRenderSettings.this.updateInputs();
			}
		});
		this.screen = container;
		this.replayHandler = replayHandler;
		this.timeline = timeline;
		String json = "{}";

		try {
			json = new String(Files.readAllBytes(this.getSettingsPath()), StandardCharsets.UTF_8);
		} catch (FileNotFoundException | NoSuchFileException var8) {
		} catch (IOException var9) {
			ReplayModRender.LOGGER.error("Reading render settings:", var9);
		}

		RenderSettings settings = null;

		try {
			settings = (RenderSettings) (new Gson()).fromJson(json, RenderSettings.class);
		} catch (JsonSyntaxException var7) {
			ReplayModRender.LOGGER.error("Parsing render settings:", var7);
			ReplayModRender.LOGGER.error("Raw JSON: {}", json);
		}

		if (settings == null) {
			settings = new RenderSettings();
		}

		this.load(settings);
	}

	protected void updateInputs() {
		RenderSettings.RenderMethod renderMethod = (RenderSettings.RenderMethod) this.renderMethodDropdown
				.getSelectedValue();
		this.videoWidth.setEnabled(!renderMethod.hasFixedAspectRatio());
		String resolutionError = this.updateResolution();
		if (resolutionError == null) {
			((GuiButton) this.queueButton.setEnabled()).setTooltip((GuiElement) null);
			this.videoWidth.setTextColor(Colors.WHITE);
			this.videoHeight.setTextColor(Colors.WHITE);
		} else {
			((GuiButton) this.queueButton.setDisabled())
					.setTooltip((new GuiTooltip()).setI18nText(resolutionError, new Object[0]));
			this.videoWidth.setTextColor(Colors.RED);
			this.videoHeight.setTextColor(Colors.RED);
		}

		String[] compatError = VideoRenderer.checkCompat(this.save(false));
		if (resolutionError != null) {
			((GuiButton) this.renderButton.setDisabled())
					.setTooltip((new GuiTooltip()).setI18nText(resolutionError, new Object[0]));
		} else if (compatError != null) {
			((GuiButton) this.renderButton.setDisabled()).setTooltip((new GuiTooltip()).setText(compatError));
		} else {
			((GuiButton) this.renderButton.setEnabled()).setTooltip((GuiElement) null);
		}

		if (((RenderSettings.EncodingPreset) this.encodingPresetDropdown.getSelectedValue()).hasBitrateSetting()) {
			this.bitRateField.setEnabled();
			this.bitRateUnit.setEnabled();
		} else {
			this.bitRateField.setDisabled();
			this.bitRateUnit.setDisabled();
		}

		switch (renderMethod) {
		case CUBIC:
		case EQUIRECTANGULAR:
		case ODS:
			this.stabilizePanel.invokeAll(IGuiCheckbox.class, GuiElement::setEnabled);
			break;
		default:
			this.stabilizePanel.invokeAll(IGuiCheckbox.class, GuiElement::setDisabled);
		}

		this.sphericalFovSlider.setEnabled(renderMethod.isSpherical());
		if (((RenderSettings.EncodingPreset) this.encodingPresetDropdown.getSelectedValue()).getFileExtension()
				.equals("mp4") && renderMethod.isSpherical()) {
			((GuiCheckbox) this.injectSphericalMetadata.setEnabled()).setTooltip((GuiElement) null);
		} else {
			((GuiCheckbox) this.injectSphericalMetadata.setDisabled())
					.setTooltip(((GuiTooltip) (new GuiTooltip()).setColor(Colors.RED))
							.setI18nText("replaymod.gui.rendersettings.sphericalmetadata.error", new Object[0]));
		}

		boolean isEXR = this.encodingPresetDropdown.getSelectedValue() == RenderSettings.EncodingPreset.EXR;
		boolean isPNG = this.encodingPresetDropdown.getSelectedValue() == RenderSettings.EncodingPreset.PNG;
		boolean isBlend = renderMethod == RenderSettings.RenderMethod.BLEND;
		boolean isFFmpeg = !isBlend && !isEXR && !isPNG;
		if (isBlend) {
			this.videoWidth.setDisabled();
			this.videoHeight.setDisabled();
		}

		this.encodingPresetDropdown.setEnabled(!isBlend);
		this.exportCommand.setEnabled(isFFmpeg);
		this.exportArguments.setEnabled(isFFmpeg);
		this.antiAliasingDropdown.setEnabled(isFFmpeg);
		if (!isEXR && !isPNG) {
			((GuiCheckbox) this.depthMap.setDisabled())
					.setTooltip(((GuiTooltip) (new GuiTooltip()).setColor(Colors.RED))
							.setI18nText("replaymod.gui.rendersettings.depthmap.only_exr_or_png", new Object[0]));
		} else {
			((GuiCheckbox) this.depthMap.setEnabled()).setTooltip((GuiElement) null);
		}

		boolean commandChanged = !this.exportCommand.getText().isEmpty();
		boolean argsChanged = !((RenderSettings.EncodingPreset) this.encodingPresetDropdown.getSelectedValue())
				.getValue().equals(this.exportArguments.getText());
		this.exportReset.setEnabled(commandChanged || argsChanged);
	}

	protected String updateResolution() {
		RenderSettings.EncodingPreset preset = (RenderSettings.EncodingPreset) this.encodingPresetDropdown
				.getSelectedValue();
		RenderSettings.RenderMethod method = (RenderSettings.RenderMethod) this.renderMethodDropdown.getSelectedValue();
		int videoHeight = this.videoHeight.getInteger();
		int videoWidth;
		if (method.hasFixedAspectRatio()) {
			if (method == RenderSettings.RenderMethod.CUBIC && videoHeight % 3 != 0) {
				return "replaymod.gui.rendersettings.customresolution.warning.cubic.height";
			}

			int sphericalFov = 120 + this.sphericalFovSlider.getValue() * 5;
			videoWidth = this.videoWidthForHeight(method, videoHeight, sphericalFov, sphericalFov);
			this.videoWidth.setValue(videoWidth);
		} else {
			videoWidth = this.videoWidth.getInteger();
		}

		if (!this.exportArguments.getText().equals(preset.getValue()) || !preset.isYuv420()
				|| videoWidth % 2 == 0 && videoHeight % 2 == 0) {
			return null;
		} else {
			return method == RenderSettings.RenderMethod.CUBIC
					? "replaymod.gui.rendersettings.customresolution.warning.yuv420.cubic"
					: "replaymod.gui.rendersettings.customresolution.warning.yuv420";
		}
	}

	protected int videoWidthForHeight(RenderSettings.RenderMethod method, int height, int sphericalFovX,
			int sphericalFovY) {
		if (method.isSpherical()) {
			if (sphericalFovY < 180) {
				height = Math.round((float) (height * 180) / (float) sphericalFovY);
			}

			int width = height * 2;
			if (sphericalFovX < 360) {
				width = Math.round((float) width * (float) sphericalFovX / 360.0F);
			}

			if (method == RenderSettings.RenderMethod.ODS) {
				width = Math.round((float) width / 2.0F);
			}

			return width;
		} else if (method == RenderSettings.RenderMethod.CUBIC) {
			Preconditions.checkArgument(height % 3 == 0);
			return height / 3 * 4;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void load(RenderSettings settings) {
		if (settings.getRenderMethod().isSupported()) {
			this.renderMethodDropdown.setSelected(settings.getRenderMethod());
		}

		RenderSettings.EncodingPreset encodingPreset = settings.getEncodingPreset();
		boolean invalidEncodingPreset = encodingPreset == null || !encodingPreset.isSupported();
		if (invalidEncodingPreset) {
			encodingPreset = (new RenderSettings()).getEncodingPreset();
		}

		this.encodingPresetDropdown.setSelected(encodingPreset);
		this.videoWidth.setValue(settings.getTargetVideoWidth());
		this.videoHeight.setValue(settings.getTargetVideoHeight());
		this.frameRateSlider.setValue(settings.getFramesPerSecond() - 10);
		if (settings.getBitRate() % 1048576 == 0) {
			this.bitRateField.setValue(settings.getBitRate() >> 20);
			this.bitRateUnit.setSelected(2);
		} else if (settings.getBitRate() % 1024 == 0) {
			this.bitRateField.setValue(settings.getBitRate() >> 10);
			this.bitRateUnit.setSelected(1);
		} else {
			this.bitRateField.setValue(settings.getBitRate());
			this.bitRateUnit.setSelected(0);
		}

		File savedOutputFile = settings.getOutputFile();
		String exportArguments;
		if (savedOutputFile != null && getParentFile(savedOutputFile).exists()) {
			if (savedOutputFile.exists()) {
				exportArguments = this.generateOutputFile(encodingPreset).getName();
				boolean isFolder = savedOutputFile.isDirectory() && !savedOutputFile.getName().endsWith(".exr");
				this.outputFile = new File(isFolder ? savedOutputFile : getParentFile(savedOutputFile),
						exportArguments);
				this.userDefinedOutputFileName = false;
			} else {
				this.outputFile = this.conformExtension(savedOutputFile, encodingPreset);
				this.userDefinedOutputFileName = true;
			}
		} else {
			this.outputFile = this.generateOutputFile(encodingPreset);
			this.userDefinedOutputFileName = false;
		}

		this.outputFileButton.setLabel(this.outputFile.getName());
		this.nametagCheckbox.setChecked(settings.isRenderNameTags());
		this.alphaCheckbox.setChecked(settings.isIncludeAlphaChannel());
		this.stabilizeYaw.setChecked(settings.isStabilizeYaw());
		this.stabilizePitch.setChecked(settings.isStabilizePitch());
		this.stabilizeRoll.setChecked(settings.isStabilizeRoll());
		if (settings.getChromaKeyingColor() == null) {
			this.chromaKeyingCheckbox.setChecked(false);
			this.chromaKeyingColor.setColor(Colors.GREEN);
		} else {
			this.chromaKeyingCheckbox.setChecked(true);
			this.chromaKeyingColor.setColor(settings.getChromaKeyingColor());
		}

		this.sphericalFovSlider.setValue((settings.getSphericalFovX() - 120) / 5);
		this.injectSphericalMetadata.setChecked(settings.isInjectSphericalMetadata());
		this.depthMap.setChecked(settings.isDepthMap());
		this.cameraPathExport.setChecked(settings.isCameraPathExport());
		this.antiAliasingDropdown.setSelected(settings.getAntiAliasing());
		this.exportCommand.setText(settings.getExportCommand());
		exportArguments = settings.getExportArguments();
		if (exportArguments == null || settings.getEncodingPreset() == null || invalidEncodingPreset) {
			exportArguments = encodingPreset.getValue();
		}

		this.exportArguments.setText(exportArguments);
		this.updateInputs();
	}

	public RenderSettings save(boolean serialize) {
		int sphericalFov = 120 + this.sphericalFovSlider.getValue() * 5;
		return new RenderSettings((RenderSettings.RenderMethod) this.renderMethodDropdown.getSelectedValue(),
				(RenderSettings.EncodingPreset) this.encodingPresetDropdown.getSelectedValue(),
				this.videoWidth.getInteger(), this.videoHeight.getInteger(), this.frameRateSlider.getValue() + 10,
				this.bitRateField.getInteger() << 10 * this.bitRateUnit.getSelected(),
				serialize && !this.userDefinedOutputFileName ? getParentFile(this.outputFile) : this.outputFile,
				this.nametagCheckbox.isChecked(), this.alphaCheckbox.isChecked(),
				this.stabilizeYaw.isChecked() && (serialize || this.stabilizeYaw.isEnabled()),
				this.stabilizePitch.isChecked() && (serialize || this.stabilizePitch.isEnabled()),
				this.stabilizeRoll.isChecked() && (serialize || this.stabilizeRoll.isEnabled()),
				this.chromaKeyingCheckbox.isChecked() ? this.chromaKeyingColor.getColor() : null, sphericalFov,
				Math.min(180, sphericalFov),
				this.injectSphericalMetadata.isChecked() && (serialize || this.injectSphericalMetadata.isEnabled()),
				this.depthMap.isChecked() && (serialize || this.depthMap.isEnabled()),
				this.cameraPathExport.isChecked(),
				!serialize && !this.antiAliasingDropdown.isEnabled() ? RenderSettings.AntiAliasing.NONE
						: (RenderSettings.AntiAliasing) this.antiAliasingDropdown.getSelectedValue(),
				this.exportCommand.getText(), this.exportArguments.getText(), Screen.hasControlDown());
	}

	protected File generateOutputFile(RenderSettings.EncodingPreset encodingPreset) {
		String fileName = (new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")).format(new Date());
		File folder = ReplayModRender.instance.getVideoFolder();
		return new File(folder, fileName + "." + encodingPreset.getFileExtension());
	}

	public AbstractGuiScreen<?> getScreen() {
		return this.screen;
	}

	public void setOutputFileBaseName(String base) {
		RenderSettings.EncodingPreset preset = (RenderSettings.EncodingPreset) this.encodingPresetDropdown
				.getSelectedValue();
		File file = new File(getParentFile(this.outputFile), base + "." + preset.getFileExtension());

		try {
			file.toPath();
			this.outputFile = file;
			this.outputFileButton.setLabel(file.getName());
		} catch (InvalidPathException var5) {
			this.setOutputFileBaseName("filename_invalid_" + base.hashCode());
		}

	}

	protected File conformExtension(File file, RenderSettings.EncodingPreset preset) {
		String name = file.getName();
		if (name.contains(".")) {
			name = name.substring(0, name.lastIndexOf(46));
		}

		return new File(getParentFile(file), name + "." + preset.getFileExtension());
	}

	protected Path getSettingsPath() {
		return ReplayModRender.instance.getRenderSettingsPath();
	}

	public void open() {
		super.open();
	}

	public void close() {
		RenderSettings settings = this.save(true);
		String json = (new Gson()).toJson(settings);

		try {
			Files.write(this.getSettingsPath(), json.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
		} catch (IOException var4) {
			ReplayModRender.LOGGER.error("Saving render settings:", var4);
		}

		super.close();
	}

	public ReplayHandler getReplayHandler() {
		return this.replayHandler;
	}

	protected GuiRenderSettings getThis() {
		return this;
	}

	public static GuiScreen createBaseScreen() {
		GuiScreen screen = new GuiScreen();
		screen.setBackground(AbstractGuiScreen.Background.NONE);
		return screen;
	}

	private static File getParentFile(File file) {
		File parent = file.getParentFile();
		return parent == null ? new File(".") : parent;
	}
}

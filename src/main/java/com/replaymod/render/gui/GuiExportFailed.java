package com.replaymod.render.gui;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import com.replaymod.core.ReplayMod;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.render.FFmpegWriter;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.ReplayModRender;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.screens.Screen;

public class GuiExportFailed extends GuiScreen {
	private final GuiLabel logLabel = (GuiLabel) (new GuiLabel(this))
			.setI18nText("replaymod.gui.rendering.error.ffmpeglog", new Object[0]);
	private final GuiVerticalList logList = (GuiVerticalList) (new GuiVerticalList(this)).setDrawShadow(true);
	private final GuiButton resetButton = (GuiButton) ((GuiButton) (new GuiButton()).setI18nLabel("gui.yes",
			new Object[0])).setSize(100, 20);
	private final GuiButton abortButton = (GuiButton) ((GuiButton) (new GuiButton()).setI18nLabel("gui.no",
			new Object[0])).setSize(100, 20);
	private final GuiPanel info;

	public static GuiExportFailed tryToRecover(FFmpegWriter.FFmpegStartupException e,
			Consumer<RenderSettings> doRestart) {
		ReplayModRender.LOGGER.error("Rendering video:", e);
		RenderSettings settings = e.getSettings();
		if (settings.getEncodingPreset().getValue().equals(settings.getExportArguments())) {
			CrashReport crashReport = CrashReport.forThrowable(e, "Exporting video");
			CrashReportCategory details = crashReport.addCategory("Export details");
			Objects.requireNonNull(settings);
			details.setDetail("Settings", settings::toString);
			Objects.requireNonNull(e);
			details.setDetail("FFmpeg log", e::getLog);
			throw new ReportedException(crashReport);
		} else {
			GuiExportFailed gui = new GuiExportFailed(e, doRestart);
			gui.display();
			return gui;
		}
	}

	public GuiExportFailed(FFmpegWriter.FFmpegStartupException e, Consumer<RenderSettings> doRestart) {
		this.info = (GuiPanel) ((GuiPanel) (new GuiPanel(this)).setLayout((new VerticalLayout()).setSpacing(4)))
				.addElements(new VerticalLayout.Data(0.5D), new GuiElement[] {
						(new GuiLabel()).setI18nText("replaymod.gui.rendering.error.ffmpegargs.1", new Object[0]),
						(new GuiLabel()).setI18nText("replaymod.gui.rendering.error.ffmpegargs.2", new Object[0]),
						new GuiLabel(),
						((GuiPanel) (new GuiPanel())
								.setLayout((new HorizontalLayout(HorizontalLayout.Alignment.CENTER)).setSpacing(5)))
								.addElements((LayoutData) null,
										new GuiElement[] { this.resetButton, this.abortButton }) });
		this.setLayout(new CustomLayout<GuiScreen>() {
			protected void layout(GuiScreen container, int width, int height) {
				this.pos(GuiExportFailed.this.info, width / 2 - this.width(GuiExportFailed.this.info) / 2,
						(height / 2 - this.height(GuiExportFailed.this.info) - 30) / 2 + 30);
				this.pos(GuiExportFailed.this.logLabel, width / 2 - this.width(GuiExportFailed.this.logLabel) / 2,
						height / 2 + 4);
				this.pos(GuiExportFailed.this.logList, 10,
						this.y(GuiExportFailed.this.logLabel) + this.height(GuiExportFailed.this.logLabel) + 4);
				this.size(GuiExportFailed.this.logList, width - 10 - this.x(GuiExportFailed.this.logList),
						height - 10 - this.y(GuiExportFailed.this.logList));
			}
		});
		this.setTitle((GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.rendering.error.title", new Object[0]));
		this.setBackground(AbstractGuiScreen.Background.DIRT);
		this.logList.getListPanel().addElements((LayoutData) null,
				(GuiElement[]) Arrays.stream(e.getLog().replace("\t", "    ").split("\n")).map((l) -> {
					return (GuiLabel) (new GuiLabel()).setText(l);
				}).toArray((x$0) -> {
					return new GuiElement[x$0];
				}));
		this.resetButton.onClick(() -> {
			ReplayMod.instance.runLater(() -> {
				RenderSettings oldSettings = e.getSettings();
				doRestart.accept(new RenderSettings(oldSettings.getRenderMethod(), oldSettings.getEncodingPreset(),
						oldSettings.getVideoWidth(), oldSettings.getVideoHeight(), oldSettings.getFramesPerSecond(),
						oldSettings.getBitRate(), oldSettings.getOutputFile(), oldSettings.isRenderNameTags(),
						oldSettings.isIncludeAlphaChannel(), oldSettings.isStabilizeYaw(),
						oldSettings.isStabilizePitch(), oldSettings.isStabilizeRoll(),
						oldSettings.getChromaKeyingColor(), oldSettings.getSphericalFovX(),
						oldSettings.getSphericalFovY(), oldSettings.isInjectSphericalMetadata(),
						oldSettings.isDepthMap(), oldSettings.isCameraPathExport(), oldSettings.getAntiAliasing(),
						oldSettings.getExportCommand(), oldSettings.getEncodingPreset().getValue(),
						oldSettings.isHighPerformance()));
			});
		});
		this.abortButton.onClick(() -> {
			this.getMinecraft().setScreen((Screen) null);
		});
	}
}

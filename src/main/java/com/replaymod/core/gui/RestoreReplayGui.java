package com.replaymod.core.gui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiProgressBar;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.io.ReplayOutputStream;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;

import net.minecraft.CrashReport;

public class RestoreReplayGui extends AbstractGuiScreen<RestoreReplayGui> {
	private static Logger LOGGER = LogManager.getLogger();
	public final GuiScreen parent;
	public final File file;
	public final GuiPanel textPanel = (GuiPanel) (new GuiPanel()).setLayout((new VerticalLayout()).setSpacing(3));
	public final GuiPanel buttonPanel = (GuiPanel) (new GuiPanel()).setLayout((new HorizontalLayout()).setSpacing(5));
	public final GuiPanel contentPanel;
	public final GuiButton yesButton;
	public final GuiButton noButton;
	private final ReplayMod core;

	public RestoreReplayGui(ReplayMod core, GuiScreen parent, File file) {
		this.contentPanel = (GuiPanel) ((GuiPanel) (new GuiPanel(this)).addElements(new VerticalLayout.Data(0.5D),
				new GuiElement[] { this.textPanel, this.buttonPanel }))
				.setLayout((new VerticalLayout()).setSpacing(20));
		this.yesButton = (GuiButton) ((GuiButton) (new GuiButton(this.buttonPanel)).setSize(150, 20))
				.setI18nLabel("gui.yes", new Object[0]);
		this.noButton = (GuiButton) ((GuiButton) (new GuiButton(this.buttonPanel)).setSize(150, 20))
				.setI18nLabel("gui.no", new Object[0]);
		this.core = core;
		this.parent = parent;
		this.file = file;
		this.textPanel.addElements(new VerticalLayout.Data(0.5D),
				new GuiElement[] { (new GuiLabel()).setI18nText("replaymod.gui.restorereplay1", new Object[0]),
						(new GuiLabel()).setI18nText("replaymod.gui.restorereplay2",
								new Object[] { Files.getNameWithoutExtension(file.getName()) }),
						(new GuiLabel()).setI18nText("replaymod.gui.restorereplay3", new Object[0]) });
		LOGGER.info("Found partially saved replay, offering recovery: " + file);
		this.yesButton.onClick(() -> {
			LOGGER.info("Attempting recovery: " + file);
			this.recoverInBackground();
			parent.display();
		});
		this.noButton.onClick(() -> {
			LOGGER.info("Recovery rejected, marking for deletion: " + file);

			try {
				File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
				File deleted = new File(file.getParentFile(), file.getName() + ".del");
				if (deleted.exists()) {
					FileUtils.deleteDirectory(deleted);
				}

				Files.move(tmp, deleted);
			} catch (IOException var4) {
				var4.printStackTrace();
			}

			parent.display();
		});
		this.setLayout(new CustomLayout<RestoreReplayGui>() {
			protected void layout(RestoreReplayGui container, int width, int height) {
				this.pos(RestoreReplayGui.this.contentPanel,
						width / 2 - this.width(RestoreReplayGui.this.contentPanel) / 2,
						height / 2 - this.height(RestoreReplayGui.this.contentPanel) / 2);
			}
		});
	}

	protected RestoreReplayGui getThis() {
		return this;
	}

	private void recoverInBackground() {
		GuiLabel label = (GuiLabel) ((GuiLabel) (new GuiLabel()).setI18nText("replaymod.gui.replaysaving.title",
				new Object[0])).setColor(Colors.BLACK);
		GuiProgressBar progressBar = (GuiProgressBar) (new GuiProgressBar()).setHeight(14);
		GuiPanel savingProcess = (GuiPanel) ((GuiPanel) (new GuiPanel()).setLayout(new VerticalLayout()))
				.addElements(new VerticalLayout.Data(0.5D), new GuiElement[] { label, progressBar });
		(new Thread(() -> {
			this.core.runLater(() -> {
				this.core.getBackgroundProcesses().addProcess(savingProcess);
			});

			try {
				Objects.requireNonNull(progressBar);
				this.tryRecover(progressBar::setProgress);
			} catch (IOException var8) {
				LOGGER.error("Recovering replay file:", var8);
				CrashReport crashReport = CrashReport.forThrowable(var8, "Recovering replay file");
				this.core.runLater(() -> {
					Utils.error(LOGGER, VanillaGuiScreen.wrap(this.getMinecraft().screen), crashReport, () -> {
					});
				});
			} finally {
				this.core.runLater(() -> {
					this.core.getBackgroundProcesses().removeProcess(savingProcess);
				});
			}

		})).start();
	}

	private void tryRecover(Consumer<Float> progress) throws IOException {
		ReplayFile replayFile = ReplayMod.instance.files.open(this.file.toPath());
		replayFile.save();
		progress.accept(0.4F);
		ReplayMetaData metaData = replayFile.getMetaData();
		if (metaData != null && metaData.getDuration() == 0) {
			try {
				ReplayInputStream in = replayFile.getPacketData(MCVer.getPacketTypeRegistry(true));

				try {
					ReplayOutputStream out = replayFile.writePacketData();

					try {
						while (true) {
							int time = com.replaymod.replaystudio.util.Utils.readInt(in);
							int length = com.replaymod.replaystudio.util.Utils.readInt(in);
							if (time == -1 || length == -1) {
								break;
							}

							byte[] buf = new byte[length];
							IOUtils.readFully(in, buf);
							metaData.setDuration(time);
							com.replaymod.replaystudio.util.Utils.writeInt(out, time);
							com.replaymod.replaystudio.util.Utils.writeInt(out, length);
							out.write(buf);
						}
					} catch (Throwable var13) {
						if (out != null) {
							try {
								out.close();
							} catch (Throwable var11) {
								var13.addSuppressed(var11);
							}
						}

						throw var13;
					}

					if (out != null) {
						out.close();
					}
				} catch (Throwable var14) {
					if (in != null) {
						try {
							in.close();
						} catch (Throwable var10) {
							var14.addSuppressed(var10);
						}
					}

					throw var14;
				}

				if (in != null) {
					in.close();
				}
			} catch (Throwable var15) {
				var15.printStackTrace();
			}

			OutputStream out = replayFile.write("metaData.json");

			try {
				metaData.setGenerator(metaData.getGenerator() + "(+ ReplayMod Replay Recovery)");
				String json = (new Gson()).toJson(metaData);
				out.write(json.getBytes());
			} catch (Throwable var12) {
				if (out != null) {
					try {
						out.close();
					} catch (Throwable var9) {
						var12.addSuppressed(var9);
					}
				}

				throw var12;
			}

			if (out != null) {
				out.close();
			}
		}

		progress.accept(0.6F);
		replayFile.save();
		progress.accept(0.9F);
		replayFile.close();
		progress.accept(1.0F);
	}
}

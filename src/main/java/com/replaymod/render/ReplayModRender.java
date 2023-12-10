package com.replaymod.render;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.replaymod.core.Module;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.render.utils.RenderJob;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.events.ReplayClosedCallback;
import com.replaymod.replay.events.ReplayOpenedCallback;
import com.replaymod.replaystudio.replay.ReplayFile;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

public class ReplayModRender extends EventRegistrations implements Module {
	public static ReplayModRender instance;
	private ReplayMod core;
	public static Logger LOGGER = LogManager.getLogger();
	private ReplayFile replayFile;
	private final List<RenderJob> renderQueue;

	public ReplayModRender(ReplayMod core) {
		instance = this;
		this.renderQueue = new ArrayList();
		this.on(ReplayOpenedCallback.EVENT, this::onReplayOpened);
		this.on(ReplayClosedCallback.EVENT, (replayHandler) -> {
			this.onReplayClosed();
		});
		this.core = core;
		core.getSettingsRegistry().register(Setting.class);
	}

	public ReplayMod getCore() {
		return this.core;
	}

	public void initClient() {
		this.register();
	}

	public File getVideoFolder() {
		String path = (String) this.core.getSettingsRegistry().get(Setting.RENDER_PATH);
		File folder = new File(path.startsWith("./") ? this.core.getMinecraft().gameDirectory : null, path);

		try {
			FileUtils.forceMkdir(folder);
			return folder;
		} catch (IOException var4) {
			throw new ReportedException(CrashReport.forThrowable(var4, "Cannot create video folder."));
		}
	}

	public Path getRenderSettingsPath() {
		return this.core.getMinecraft().gameDirectory.toPath().resolve("config/replaymod-rendersettings.json");
	}

	public List<RenderJob> getRenderQueue() {
		return this.renderQueue;
	}

	private void onReplayOpened(ReplayHandler replayHandler) {
		this.replayFile = replayHandler.getReplayFile();

		try {
			this.renderQueue.addAll(RenderJob.readQueue(this.replayFile));
		} catch (IOException var3) {
			throw new ReportedException(CrashReport.forThrowable(var3, "Reading timeline"));
		}
	}

	private void onReplayClosed() {
		this.renderQueue.clear();
		this.replayFile = null;
	}

	public void saveRenderQueue() {
		try {
			RenderJob.writeQueue(this.replayFile, this.renderQueue);
		} catch (IOException var4) {
			var4.printStackTrace();
			VanillaGuiScreen screen = VanillaGuiScreen.wrap(this.getCore().getMinecraft().screen);
			CrashReport report = CrashReport.forThrowable(var4, "Reading timeline");
			Utils.error(LOGGER, screen, report, () -> {
			});
		}

	}
}

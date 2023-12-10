package com.replaymod.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.replaymod.compat.ReplayModCompat;
import com.replaymod.core.files.ReplayFilesService;
import com.replaymod.core.files.ReplayFoldersService;
import com.replaymod.core.gui.GuiBackgroundProcesses;
import com.replaymod.core.gui.GuiReplaySettings;
import com.replaymod.core.versions.MCVer;
import com.replaymod.core.versions.scheduler.Scheduler;
import com.replaymod.core.versions.scheduler.SchedulerImpl;
import com.replaymod.editor.ReplayModEditor;
import com.replaymod.extras.ReplayModExtras;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.render.ReplayModRender;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.version.ProtocolVersion;
import com.replaymod.replaystudio.studio.ReplayStudio;
import com.replaymod.replaystudio.util.I18n;
import com.replaymod.simplepathing.ReplayModSimplePathing;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FolderPackResources;

public class ReplayMod implements Module, Scheduler {
	public static final String MOD_ID = "replaymod";
	public static final ResourceLocation TEXTURE = new ResourceLocation("replaymod", "replay_gui.png");
	public static final int TEXTURE_SIZE = 256;
	public static final ResourceLocation LOGO_FAVICON = new ResourceLocation("replaymod", "favicon_logo.png");
	private static final Minecraft mc = MCVer.getMinecraft();
	private final ReplayModBackend backend;
	private final SchedulerImpl scheduler = new SchedulerImpl();
	private final KeyBindingRegistry keyBindingRegistry = new KeyBindingRegistry();
	private final SettingsRegistry settingsRegistry = new SettingsRegistry();
	public static ReplayMod instance;
	private final List<Module> modules;
	private final GuiBackgroundProcesses backgroundProcesses;
	public final ReplayFoldersService folders;
	public final ReplayFilesService files;
	private boolean minimalMode;
	public static final FolderPackResources jGuiResourcePack = createJGuiResourcePack();
	public static final String JGUI_RESOURCE_PACK_NAME = "replaymod_jgui";

	public ReplayMod(ReplayModBackend backend) {
		this.settingsRegistry.register(Setting.class);
		instance = this;
		this.modules = new ArrayList();
		this.backgroundProcesses = new GuiBackgroundProcesses();
		this.folders = new ReplayFoldersService(this.settingsRegistry);
		this.files = new ReplayFilesService(this.folders);
		this.backend = backend;
		I18n.setI18n(I18n::format);
		if (!ProtocolVersion.isRegistered(MCVer.getProtocolVersion())
				&& !Boolean.parseBoolean(System.getProperty("replaymod.skipversioncheck", "false"))) {
			this.minimalMode = true;
		}

		this.modules.add(this);
		this.modules.add(new ReplayModRecording(this));
		ReplayModReplay replayModule = new ReplayModReplay(this);
		this.modules.add(replayModule);
		this.modules.add(new ReplayModRender(this));
		this.modules.add(new ReplayModSimplePathing(this));
		this.modules.add(new ReplayModEditor(this));
		this.modules.add(new ReplayModExtras(this));
		this.modules.add(new ReplayModCompat());
		this.settingsRegistry.register();
	}

	public KeyBindingRegistry getKeyBindingRegistry() {
		return this.keyBindingRegistry;
	}

	public SettingsRegistry getSettingsRegistry() {
		return this.settingsRegistry;
	}

	private static FolderPackResources createJGuiResourcePack() {
		File folder = new File("../jGui/src/main/resources");
		if (!folder.exists()) {
			folder = new File("../../../jGui/src/main/resources");
			if (!folder.exists()) {
				return null;
			}
		}

		return new FolderPackResources(folder) {
			public String getName() {
				return "replaymod_jgui";
			}

			protected InputStream getResource(String resourceName) throws IOException {
				try {
					return super.getResource(resourceName);
				} catch (IOException var3) {
					if ("pack.mcmeta".equals(resourceName)) {
						return new ByteArrayInputStream(this.generatePackMeta());
					} else {
						throw var3;
					}
				}
			}

			private byte[] generatePackMeta() {
				int version = 4;
				return ("{\"pack\": {\"description\": \"dummy pack for jGui resources in dev-env\", \"pack_format\": "
						+ version + "}}").getBytes(StandardCharsets.UTF_8);
			}
		};
	}

	void initModules() {
		this.modules.forEach(Module::initCommon);
		this.modules.forEach(Module::initClient);
		this.modules.forEach((m) -> {
			m.registerKeyBindings(this.keyBindingRegistry);
		});
	}

	public void registerKeyBindings(KeyBindingRegistry registry) {
		registry.registerKeyBinding("replaymod.input.settings", 0, () -> {
			(new GuiReplaySettings((Screen) null, this.settingsRegistry)).display();
		}, false);
	}

	public void initClient() {
		this.backgroundProcesses.register();
		this.keyBindingRegistry.register();
		this.runPostStartup(() -> {
			this.files.initialScan(this);
		});
	}

	public void runSync(Runnable runnable) throws InterruptedException, ExecutionException, TimeoutException {
		this.scheduler.runSync(runnable);
	}

	public void runPostStartup(Runnable runnable) {
		this.scheduler.runPostStartup(runnable);
	}

	public void runLaterWithoutLock(Runnable runnable) {
		this.scheduler.runLaterWithoutLock(runnable);
	}

	public void runLater(Runnable runnable) {
		this.scheduler.runLater(runnable);
	}

	public void runTasks() {
		this.scheduler.runTasks();
	}

	public String getVersion() {
		return this.backend.getVersion();
	}

	public String getMinecraftVersion() {
		return this.backend.getMinecraftVersion();
	}

	public boolean isModLoaded(String id) {
		return this.backend.isModLoaded(id);
	}

	public Minecraft getMinecraft() {
		return mc;
	}

	public void printInfoToChat(String message, Object... args) {
		this.printToChat(false, message, args);
	}

	public void printWarningToChat(String message, Object... args) {
		this.printToChat(true, message, args);
	}

	private void printToChat(boolean warning, String message, Object... args) {
		if ((Boolean) this.getSettingsRegistry().get(Setting.NOTIFICATIONS)) {
			Style coloredDarkGray = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
			Style coloredGold = Style.EMPTY.withColor(ChatFormatting.GOLD);
			Style alert = Style.EMPTY.withColor(warning ? ChatFormatting.RED : ChatFormatting.DARK_GREEN);
			Component text = Component.literal("[").setStyle(coloredDarkGray)
					.append(Component.translatable("replaymod.title").setStyle(coloredGold))
					.append(Component.literal("] ")).append(Component.translatable(message, args).setStyle(alert));
			mc.gui.getChat().addMessage(text);
		}

	}

	public GuiBackgroundProcesses getBackgroundProcesses() {
		return this.backgroundProcesses;
	}

	public static boolean isMinimalMode() {
		return instance.minimalMode;
	}

	public static boolean isCompatible(int fileFormatVersion, int protocolVersion) {
		if (isMinimalMode()) {
			return protocolVersion == MCVer.getProtocolVersion();
		} else {
			return (new ReplayStudio()).isCompatible(fileFormatVersion, protocolVersion, MCVer.getProtocolVersion());
		}
	}
}

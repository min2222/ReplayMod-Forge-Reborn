package com.replaymod.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.common.net.PercentEscaper;
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
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.studio.ReplayStudio;
import com.replaymod.replaystudio.util.I18n;
import com.replaymod.simplepathing.ReplayModSimplePathing;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

    {
        settingsRegistry.register(Setting.class);
    }

    {
        instance = this;
    }

    public static ReplayMod instance;

    private final List<Module> modules = new ArrayList<>();

    private final GuiBackgroundProcesses backgroundProcesses = new GuiBackgroundProcesses();
    public final ReplayFoldersService folders = new ReplayFoldersService(settingsRegistry);
    public final ReplayFilesService files = new ReplayFilesService(folders);

    /**
     * Whether the current MC version is supported by the embedded ReplayStudio version.
     * If this is not the case (i.e. if this is variable true), any feature of the RM which depends on the ReplayStudio
     * lib will be disabled.
     * <p>
     * Only supported on Fabric builds, i.e. will always be false / crash the game with Forge/pre-1.14 builds.
     * (specifically the code below and MCVer#getProtocolVersion make this assumption)
     */
    private boolean minimalMode;

    public ReplayMod(ReplayModBackend backend) {
        this.backend = backend;

        I18n.setI18n(I18n::format);

        // Check Minecraft protocol version for compatibility
        if (!ProtocolVersion.isRegistered(MCVer.getProtocolVersion()) && !Boolean.parseBoolean(System.getProperty("replaymod.skipversioncheck", "false"))) {
            minimalMode = true;
        }

        // Register all RM modules
        modules.add(this);
        modules.add(new ReplayModRecording(this));
        ReplayModReplay replayModule = new ReplayModReplay(this);
        modules.add(replayModule);
        modules.add(new ReplayModRender(this));
        modules.add(new ReplayModSimplePathing(this));
        modules.add(new ReplayModEditor(this));
        modules.add(new ReplayModExtras(this));
        modules.add(new ReplayModCompat());
        settingsRegistry.register();
    }

    public KeyBindingRegistry getKeyBindingRegistry() {
        return keyBindingRegistry;
    }

    public SettingsRegistry getSettingsRegistry() {
        return settingsRegistry;
    }

    public Path getReplayFolder() throws IOException {
        String str = getSettingsRegistry().get(Setting.RECORDING_PATH);
        return Files.createDirectories(mc.gameDirectory.toPath().resolve(str));
    }

    /**
     * Folder into which replay backups are saved before the MarkerProcessor is unleashed.
     */
    public Path getRawReplayFolder() throws IOException {
        return Files.createDirectories(getReplayFolder().resolve("raw"));
    }

    /**
     * Folder into which replays are recorded.
     * Distinct from the main folder, so they cannot be opened while they are still saving.
     */
    public Path getRecordingFolder() throws IOException {
        return Files.createDirectories(getReplayFolder().resolve("recording"));
    }

    /**
     * Folder in which replay cache files are stored.
     * Distinct from the recording folder cause people kept confusing them with recordings.
     */
    public Path getCacheFolder() throws IOException {
        String str = getSettingsRegistry().get(Setting.CACHE_PATH);
        Path path = mc.gameDirectory.toPath().resolve(str);
        Files.createDirectories(path);
        try {
            Files.setAttribute(path, "dos:hidden", true);
        } catch (UnsupportedOperationException ignored) {
        }
        return path;
    }

    private static final PercentEscaper CACHE_FILE_NAME_ENCODER = new PercentEscaper("-_ ", false);

    public Path getCachePathForReplay(Path replay) throws IOException {
        Path replayFolder = getReplayFolder();
        Path cacheFolder = getCacheFolder();
        Path relative = replayFolder.toAbsolutePath().relativize(replay.toAbsolutePath());
        return cacheFolder.resolve(CACHE_FILE_NAME_ENCODER.escape(relative.toString()));
    }

    public Path getReplayPathForCache(Path cache) throws IOException {
        String relative = URLDecoder.decode(cache.getFileName().toString(), "UTF-8");
        Path replayFolder = getReplayFolder();
        return replayFolder.resolve(relative);
    }

    public static final FolderPackResources jGuiResourcePack = createJGuiResourcePack();
    public static final String JGUI_RESOURCE_PACK_NAME = "replaymod_jgui";

    private static FolderPackResources createJGuiResourcePack() {
        File folder = new File("../jGui/src/main/resources");
        if (!folder.exists()) {
            return null;
        }
        return new FolderPackResources(folder) {
            @Override
            public String getName() {
                return JGUI_RESOURCE_PACK_NAME;
            }

            @Override
            protected InputStream getResource(String resourceName) throws IOException {
                try {
                    return super.getResource(resourceName);
                } catch (IOException e) {
                    if ("pack.mcmeta".equals(resourceName)) {
                        int version = 4;
                        return new ByteArrayInputStream(("{\"pack\": {\"description\": \"dummy pack for jGui resources in dev-env\", \"pack_format\": "
                                + version + "}}").getBytes(StandardCharsets.UTF_8));
                    }
                    throw e;
                }
            }
        };
    }

    void initModules() {
        modules.forEach(Module::initCommon);
        modules.forEach(Module::initClient);
        modules.forEach(m -> m.registerKeyMappings(keyBindingRegistry));
    }

    @Override
    public void registerKeyMappings(KeyBindingRegistry registry) {
    	registry.registerKeyMapping("replaymod.input.settings", 0, () -> {
            new GuiReplaySettings(null, settingsRegistry).display();
        }, false);
    }

    @Override
    public void initClient() {
        backgroundProcesses.register();
        keyBindingRegistry.register();

        runPostStartup(() -> files.initialScan(this));
    }

    @Override
    public void runSync(Runnable runnable) throws InterruptedException, ExecutionException, TimeoutException {
        scheduler.runSync(runnable);
    }

    @Override
    public void runPostStartup(Runnable runnable) {
        scheduler.runPostStartup(runnable);
    }

    @Override
    public void runLaterWithoutLock(Runnable runnable) {
        scheduler.runLaterWithoutLock(runnable);
    }

    @Override
    public void runLater(Runnable runnable) {
        scheduler.runLater(runnable);
    }

    @Override
    public void runTasks() {
        scheduler.runTasks();
    }

    public String getVersion() {
        return backend.getVersion();
    }

    public String getMinecraftVersion() {
        return backend.getMinecraftVersion();
    }

    public boolean isModLoaded(String id) {
        return backend.isModLoaded(id);
    }

    public Minecraft getMinecraft() {
        return mc;
    }

    public void printInfoToChat(String message, Object... args) {
        printToChat(false, message, args);
    }

    public void printWarningToChat(String message, Object... args) {
        printToChat(true, message, args);
    }

    private void printToChat(boolean warning, String message, Object... args) {
        if (getSettingsRegistry().get(Setting.NOTIFICATIONS)) {
            // Some nostalgia: "§8[§6Replay Mod§8]§r Your message goes here"
            Style coloredDarkGray = Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY);
            Style coloredGold = Style.EMPTY.applyFormat(ChatFormatting.GOLD);
            Style alert = Style.EMPTY.applyFormat(warning ? ChatFormatting.RED : ChatFormatting.DARK_GREEN);
            Component text = Component.literal("[").setStyle(coloredDarkGray)
                    .append(Component.translatable("replaymod.title").setStyle(coloredGold))
                    .append(Component.literal("] "))
                    .append(Component.translatable(message, args).setStyle(alert));
            // Send message to chat GUI
            // The ingame GUI is initialized at startup, therefore this is possible before the client is connected
            mc.gui.getChat().addMessage(text);
        }	
    }

    public GuiBackgroundProcesses getBackgroundProcesses() {
        return backgroundProcesses;
    }

    // This method is static because it depends solely on the environment, not on the actual RM instance.
    public static boolean isMinimalMode() {
        return ReplayMod.instance.minimalMode;
    }

    public static boolean isCompatible(int fileFormatVersion, int protocolVersion) {
        if (isMinimalMode()) {
            return protocolVersion == MCVer.getProtocolVersion();
        } else {
            return new ReplayStudio().isCompatible(fileFormatVersion, protocolVersion, MCVer.getProtocolVersion());
        }
    }

    public ReplayFile openReplay(Path path) throws IOException {
        return openReplay(path, path);
    }

    public ReplayFile openReplay(Path input, Path output) throws IOException {
        return new ZipReplayFile(
                new ReplayStudio(),
                input != null ? input.toFile() : null,
                output.toFile(),
                getCachePathForReplay(output).toFile()
        );
    }
}

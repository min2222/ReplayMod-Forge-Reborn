package com.replaymod.core;

import com.replaymod.gui.versions.forge.EventsAdapter;

import net.minecraft.SharedConstants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ReplayMod.MOD_ID)
public class ReplayModBackend {
	private final ReplayMod mod = new ReplayMod(this);
	private final EventsAdapter eventsAdapter = new EventsAdapter();
	public static boolean hasIris;

	public ReplayModBackend() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
	}

	public void init(FMLCommonSetupEvent event) {
		mod.initModules();
		eventsAdapter.register();
		hasIris = ModList.get().isLoaded("iris");
		// config = new Configuration(event.getSuggestedConfigurationFile());
		// config.load();
		// SettingsRegistry settingsRegistry = mod.getSettingsRegistry();
		// settingsRegistry.backend.setConfiguration(config);
		// settingsRegistry.save(); // Save default values to disk
	}

	public void onInitializeClient() {
		this.mod.initModules();
	}

	public String getVersion() {
		return "2.6.13";
	}

	public String getMinecraftVersion() {
		return SharedConstants.getCurrentVersion().getName();
	}

	public boolean isModLoaded(String id) {
		return ModList.get().isLoaded(id);
	}
}

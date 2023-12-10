package com.replaymod.core.utils;

import java.util.Collection;
import java.util.stream.Collectors;

import com.replaymod.replaystudio.data.ModInfo;

import net.minecraftforge.fml.ModList;

class ModInfoGetter {
	static Collection<ModInfo> getInstalledNetworkMods() {
		return ModList.get().getMods().stream()
				.map(mod -> new ModInfo(mod.getModId(), mod.getModId(), mod.getVersion().toString()))
				.collect(Collectors.toList());
	}
}
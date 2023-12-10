package com.replaymod.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.replaymod.replaystudio.data.ModInfo;

public class ModCompat {
	public static Collection<ModInfo> getInstalledNetworkMods() {
		return ModInfoGetter.getInstalledNetworkMods();
	}

	public static final class ModInfoDifference {
		private final Set<ModInfo> missing = new HashSet();
		private final Map<ModInfo, String> differing = new HashMap();

		public ModInfoDifference(Collection<ModInfo> requiredList) {
			Collection<ModInfo> installedList = ModCompat.getInstalledNetworkMods();
			Iterator var3 = requiredList.iterator();

			while (true) {
				label24: while (var3.hasNext()) {
					ModInfo required = (ModInfo) var3.next();
					Iterator var5 = installedList.iterator();

					while (var5.hasNext()) {
						ModInfo installed = (ModInfo) var5.next();
						if (required.getId().equals(installed.getId())) {
							if (!Objects.equals(required.getVersion(), installed.getVersion())) {
								this.differing.put(required, installed.getVersion());
							}
							continue label24;
						}
					}

					this.missing.add(required);
				}

				return;
			}
		}

		public Set<ModInfo> getMissing() {
			return Collections.unmodifiableSet(this.missing);
		}

		public Map<ModInfo, String> getDiffering() {
			return Collections.unmodifiableMap(this.differing);
		}
	}
}

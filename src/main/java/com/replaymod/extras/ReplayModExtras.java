package com.replaymod.extras;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.replaymod.core.Module;
import com.replaymod.core.ReplayMod;
import com.replaymod.extras.advancedscreenshots.AdvancedScreenshots;
import com.replaymod.extras.playeroverview.PlayerOverview;
import com.replaymod.extras.youtube.YoutubeUpload;

public class ReplayModExtras implements Module {
	public static ReplayModExtras instance;
	private static final List<Class<? extends Extra>> builtin = Arrays.asList(AdvancedScreenshots.class,
			PlayerOverview.class, YoutubeUpload.class, FullBrightness.class, QuickMode.class, HotkeyButtons.class);
	private final Map<Class<? extends Extra>, Extra> instances;
	public static Logger LOGGER = LogManager.getLogger();

	public ReplayModExtras(ReplayMod core) {
		instance = this;
		this.instances = new HashMap();
		core.getSettingsRegistry().register(Setting.class);
	}

	public void initClient() {
		Iterator var1 = builtin.iterator();

		while (var1.hasNext()) {
			Class cls = (Class) var1.next();

			try {
				Extra extra = (Extra) cls.newInstance();
				extra.register(ReplayMod.instance);
				this.instances.put(cls, extra);
			} catch (Throwable var4) {
				LOGGER.warn("Failed to load extra " + cls.getName() + ": ", var4);
			}
		}

	}

	public <T extends Extra> Optional<T> get(Class<T> cls) {
		Optional var10000 = Optional.ofNullable((Extra) this.instances.get(cls));
		Objects.requireNonNull(cls);
		return var10000.map(cls::cast);
	}
}

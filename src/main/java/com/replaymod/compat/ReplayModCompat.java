package com.replaymod.compat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.replaymod.compat.optifine.DisableFastRender;
import com.replaymod.compat.shaders.ShaderBeginRender;
import com.replaymod.core.Module;

public class ReplayModCompat implements Module {
	public static Logger LOGGER = LogManager.getLogger();

	public void initClient() {
		(new ShaderBeginRender()).register();
		(new DisableFastRender()).register();
	}
}

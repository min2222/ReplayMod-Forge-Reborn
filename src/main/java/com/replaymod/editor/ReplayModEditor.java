package com.replaymod.editor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.replaymod.core.Module;
import com.replaymod.core.ReplayMod;
import com.replaymod.editor.handler.GuiHandler;

public class ReplayModEditor implements Module {
	public static ReplayModEditor instance;
	private ReplayMod core;
	public static Logger LOGGER = LogManager.getLogger();

	public ReplayModEditor(ReplayMod core) {
		instance = this;
		this.core = core;
	}

	public void initClient() {
		(new GuiHandler()).register();
	}

	public ReplayMod getCore() {
		return this.core;
	}
}

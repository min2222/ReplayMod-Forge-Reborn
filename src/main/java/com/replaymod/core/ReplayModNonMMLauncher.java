package com.replaymod.core;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ReplayModNonMMLauncher implements IMixinConfigPlugin {
	private final Logger logger = LogManager.getLogger("replaymod/nonmm");

	public void onLoad(String mixinPackage) {
	}

	public String getRefMapperConfig() {
		return null;
	}

	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return false;
	}

	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	public List<String> getMixins() {
		try {
			if (ReplayModMixinConfigPlugin.hasClass("com.chocohead.mm.Plugin")) {
				this.logger.debug("Detected MM, they should call us...");
			} else {
				this.logger.debug("Did not detect MM, initializing ourselves...");
				(new ReplayModMMLauncher()).run();
			}

			return null;
		} catch (IOException var2) {
			throw new RuntimeException(var2);
		}
	}

	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}

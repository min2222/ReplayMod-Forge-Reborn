package com.replaymod.replay.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.replaymod.core.ReplayMod;
import com.replaymod.replay.ReplayModReplay;

import net.minecraft.client.KeyMapping;

@Mixin({ KeyMapping.class })
public class Mixin_ContextualKeyBindings {
	@Shadow
	@Final
	private static Map<String, KeyMapping> ALL;
	@Unique
	private static final List<KeyMapping> temporarilyRemoved = new ArrayList();

	@Unique
	private static Collection<KeyMapping> keyBindings() {
		return ALL.values();
	}

	@Inject(method = { "resetMapping" }, at = { @At("HEAD") })
	private static void preContextualKeyBindings(CallbackInfo ci) {
		ReplayMod mod = ReplayMod.instance;
		if (mod != null) {
			Set<KeyMapping> onlyInReplay = mod.getKeyBindingRegistry().getOnlyInReplay();
			if (ReplayModReplay.instance.getReplayHandler() != null) {
				keyBindings().removeIf((keyBinding) -> {
					Iterator var2 = onlyInReplay.iterator();

					KeyMapping exclusiveBinding;
					do {
						if (!var2.hasNext()) {
							return false;
						}

						exclusiveBinding = (KeyMapping) var2.next();
					} while (!keyBinding.equals(exclusiveBinding) || keyBinding == exclusiveBinding);

					temporarilyRemoved.add(keyBinding);
					return true;
				});
			} else {
				keyBindings().removeIf((keyBinding) -> {
					if (onlyInReplay.contains(keyBinding)) {
						temporarilyRemoved.add(keyBinding);
						return true;
					} else {
						return false;
					}
				});
			}

		}
	}

	@Inject(method = { "resetMapping" }, at = { @At("RETURN") })
	private static void postContextualKeyBindings(CallbackInfo ci) {
		Iterator var1 = temporarilyRemoved.iterator();

		while (var1.hasNext()) {
			KeyMapping keyBinding = (KeyMapping) var1.next();
			ALL.put(keyBinding.getName(), keyBinding);
		}

		temporarilyRemoved.clear();
	}
}

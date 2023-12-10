package com.replaymod.extras;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.events.PostRenderCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiImage;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiImage;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.events.ReplayOpenedCallback;
import com.replaymod.replay.gui.overlay.GuiReplayOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class FullBrightness extends EventRegistrations implements Extra {
	private ReplayMod core;
	private ReplayModReplay module;
	private final IGuiImage indicator;
	private Minecraft mc;
	private boolean active;
	private double originalGamma;

	public FullBrightness() {
		this.indicator = (IGuiImage) ((GuiImage) (new GuiImage()).setTexture(ReplayMod.TEXTURE, 90, 20, 19, 16))
				.setSize(19, 16);
		this.on(PreRenderCallback.EVENT, this::preRender);
		this.on(PostRenderCallback.EVENT, this::postRender);
		this.on(ReplayOpenedCallback.EVENT, (replayHandler) -> {
			this.updateIndicator(replayHandler.getOverlay());
		});
	}

	public void register(ReplayMod mod) throws Exception {
		this.core = mod;
		this.module = ReplayModReplay.instance;
		this.mc = mod.getMinecraft();
		mod.getKeyBindingRegistry().registerKeyBinding("replaymod.input.lighting", 90, new Runnable() {
			public void run() {
				FullBrightness.this.active = !FullBrightness.this.active;
				mod.getMinecraft().gameRenderer.tick();
				ReplayHandler replayHandler = FullBrightness.this.module.getReplayHandler();
				if (replayHandler != null) {
					FullBrightness.this.updateIndicator(replayHandler.getOverlay());
				}

			}
		}, true);
		this.register();
	}

	public FullBrightness.Type getType() {
		String str = (String) this.core.getSettingsRegistry().get(Setting.FULL_BRIGHTNESS);
		FullBrightness.Type[] var2 = FullBrightness.Type.values();
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			FullBrightness.Type type = var2[var4];
			if (type.toString().equals(str)) {
				return type;
			}
		}

		return FullBrightness.Type.Gamma;
	}

	private void preRender() {
		if (this.active && this.module.getReplayHandler() != null) {
			FullBrightness.Type type = this.getType();
			if (type == FullBrightness.Type.Gamma || type == FullBrightness.Type.Both) {
				this.originalGamma = (Double) this.mc.options.gamma().get();
				this.mc.options.gamma().set(1000.0D);
			}

			if ((type == FullBrightness.Type.NightVision || type == FullBrightness.Type.Both)
					&& this.mc.player != null) {
				this.mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE));
			}
		}

	}

	private void postRender() {
		if (this.active && this.module.getReplayHandler() != null) {
			FullBrightness.Type type = this.getType();
			if (type == FullBrightness.Type.Gamma || type == FullBrightness.Type.Both) {
				this.mc.options.gamma().set(this.originalGamma);
			}

			if ((type == FullBrightness.Type.NightVision || type == FullBrightness.Type.Both)
					&& this.mc.player != null) {
				this.mc.player.removeEffect(MobEffects.NIGHT_VISION);
			}
		}

	}

	private void updateIndicator(GuiReplayOverlay overlay) {
		if (this.active) {
			overlay.statusIndicatorPanel.addElements(new HorizontalLayout.Data(1.0D),
					new GuiElement[] { this.indicator });
		} else {
			overlay.statusIndicatorPanel.removeElement(this.indicator);
		}

	}

	static enum Type {
		Gamma, NightVision, Both;

		public String toString() {
			return "replaymod.gui.settings.fullbrightness." + this.name().toLowerCase();
		}

		// $FF: synthetic method
		private static FullBrightness.Type[] $values() {
			return new FullBrightness.Type[] { Gamma, NightVision, Both };
		}
	}
}

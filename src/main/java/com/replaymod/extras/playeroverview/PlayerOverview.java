package com.replaymod.extras.playeroverview;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.events.PreRenderHandCallback;
import com.replaymod.core.utils.Utils;
import com.replaymod.extras.Extra;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.camera.CameraEntity;
import com.replaymod.replay.events.ReplayClosedCallback;
import com.replaymod.replay.events.ReplayOpenedCallback;
import com.replaymod.replaystudio.lib.guava.base.Optional;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerOverview extends EventRegistrations implements Extra {
	private ReplayModReplay module;
	private final Set<UUID> hiddenPlayers = new HashSet();
	private boolean savingEnabled;

	public PlayerOverview() {
		this.on(ReplayOpenedCallback.EVENT, this::onReplayOpen);
		this.on(ReplayClosedCallback.EVENT, this::onReplayClose);
		this.on(PreRenderHandCallback.EVENT, this::shouldHideHand);
	}

	public void register(ReplayMod mod) throws Exception {
		this.module = ReplayModReplay.instance;
		mod.getKeyBindingRegistry().registerKeyBinding("replaymod.input.playeroverview", 66, new Runnable() {
			public void run() {
				if (PlayerOverview.this.module.getReplayHandler() != null) {
					List<Player> players = (List) mod.getMinecraft().level.players().stream().map((it) -> {
						return it;
					}).filter((it) -> {
						return !(it instanceof CameraEntity);
					}).collect(Collectors.toList());
					if (!Utils.isCtrlDown()) {
						Iterator iter = players.iterator();

						while (iter.hasNext()) {
							UUID uuid = ((Player) iter.next()).getGameProfile().getId();
							if (uuid != null && uuid.version() == 2) {
								iter.remove();
							}
						}
					}

					(new PlayerOverviewGui(PlayerOverview.this, players)).display();
				}

			}
		}, true);
		this.register();
	}

	public boolean isHidden(UUID uuid) {
		return this.hiddenPlayers.contains(uuid);
	}

	public void setHidden(UUID uuid, boolean hidden) {
		if (hidden) {
			this.hiddenPlayers.add(uuid);
		} else {
			this.hiddenPlayers.remove(uuid);
		}

	}

	private void onReplayOpen(ReplayHandler replayHandler) throws IOException {
		Optional<Set<UUID>> savedData = replayHandler.getReplayFile().getInvisiblePlayers();
		if (savedData.isPresent()) {
			this.hiddenPlayers.addAll((Collection) savedData.get());
			this.savingEnabled = true;
		} else {
			this.savingEnabled = false;
		}

	}

	private void onReplayClose(ReplayHandler replayHandler) {
		this.hiddenPlayers.clear();
	}

	private boolean shouldHideHand() {
		Entity view = this.module.getCore().getMinecraft().getCameraEntity();
		return view != null && this.isHidden(view.getUUID());
	}

	public boolean isSavingEnabled() {
		return this.savingEnabled;
	}

	public void setSavingEnabled(boolean savingEnabled) {
		this.savingEnabled = savingEnabled;
	}

	public void saveHiddenPlayers() {
		if (this.savingEnabled) {
			try {
				this.module.getReplayHandler().getReplayFile().writeInvisiblePlayers(this.hiddenPlayers);
			} catch (IOException var2) {
				var2.printStackTrace();
			}
		}

	}
}

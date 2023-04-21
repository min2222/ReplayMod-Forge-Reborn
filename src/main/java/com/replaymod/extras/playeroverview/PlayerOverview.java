package com.replaymod.extras.playeroverview;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.events.PreRenderHandCallback;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer.Keyboard;
import com.replaymod.extras.Extra;
import com.replaymod.gui.utils.EventRegistrations;
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

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private boolean savingEnabled;

    @Override
    public void register(final ReplayMod mod) throws Exception {
        this.module = ReplayModReplay.instance;

        mod.getKeyBindingRegistry().registerKeyMapping("replaymod.input.playeroverview", Keyboard.KEY_B, new Runnable() {
            @Override
            public void run() {
                if (module.getReplayHandler() != null) {
                    List<Player> players = mod.getMinecraft().level.players()
                            .stream()
                            .map(it -> (Player) it)
                            .filter(it -> !(it instanceof CameraEntity))
                            .collect(Collectors.toList());
                    if (!Utils.isCtrlDown()) {
                        // Hide all players that have an UUID v2 (commonly used for NPCs)
                        Iterator<Player> iter = players.iterator();
                        while (iter.hasNext()) {
                            UUID uuid = iter.next().getGameProfile().getId();
                            if (uuid != null && uuid.version() == 2) {
                                iter.remove();
                            }
                        }
                    }
                    new PlayerOverviewGui(PlayerOverview.this, players).display();
                }
            }
        }, true);

        register();
    }

    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }

    public void setHidden(UUID uuid, boolean hidden) {
        if (hidden) {
            hiddenPlayers.add(uuid);
        } else {
            hiddenPlayers.remove(uuid);
        }
    }

    {
        on(ReplayOpenedCallback.EVENT, this::onReplayOpen);
    }

    private void onReplayOpen(ReplayHandler replayHandler) throws IOException {
        Optional<Set<UUID>> savedData = replayHandler.getReplayFile().getInvisiblePlayers();
        if (savedData.isPresent()) {
            hiddenPlayers.addAll(savedData.get());
            savingEnabled = true;
        } else {
            savingEnabled = false;
        }
    }

    {
        on(ReplayClosedCallback.EVENT, this::onReplayClose);
    }

    private void onReplayClose(ReplayHandler replayHandler) {
        hiddenPlayers.clear();
    }

    {
        on(PreRenderHandCallback.EVENT, this::shouldHideHand);
    }

    private boolean shouldHideHand() {
        Entity view = module.getCore().getMinecraft().getCameraEntity();
        return view != null && isHidden(view.getUUID());
    }

    // See MixinRender for why this is 1.7.10 only

    public boolean isSavingEnabled() {
        return savingEnabled;
    }

    public void setSavingEnabled(boolean savingEnabled) {
        this.savingEnabled = savingEnabled;
    }

    public void saveHiddenPlayers() {
        if (savingEnabled) {
            try {
                module.getReplayHandler().getReplayFile().writeInvisiblePlayers(hiddenPlayers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.anchor.AnchorData;
import com.kawan.lotmanchors.anchor.OfflineAnchorDecay;
import com.kawan.lotmanchors.config.AnchorConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Login/logout bookkeeping and optional offline decay. */
public final class PlayerLifecycleHandler {
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        HonorificPrayerHandler.refreshSequence(player);
        AnchorData data = player.getData(LOTMAnchors.ANCHOR_DATA);
        long now = System.currentTimeMillis();

        if (AnchorConfig.offlineDecayEnabled() && data.getLastSeenEpochMs() > 0) {
            long offlineMs = Math.max(0, now - data.getLastSeenEpochMs());
            long current = data.getTotalAnchors();
            long remaining = OfflineAnchorDecay.remaining(current, offlineMs, AnchorConfig.offlineGraceHours(),
                    AnchorConfig.offlineDecayPercentPerDay(), AnchorConfig.offlineDecayIntervalHours());
            long lost = data.removeAnchors(current - remaining);
            if (lost > 0) player.sendSystemMessage(Component.literal("§c[Anchors] You lost " + lost + " Anchors for prolonged absence."));
        }
        data.setLastSeenEpochMs(now);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HonorificPrayerHandler.refreshSequence(player);
            player.getData(LOTMAnchors.ANCHOR_DATA).setLastSeenEpochMs(System.currentTimeMillis());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
            HonorificPrayerHandler.refreshSequence(player);
        }
    }
}

package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.anchor.AnchorData;
import com.kawan.lotmanchors.config.AnchorConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Login/logout bookkeeping and optional offline decay. */
public final class PlayerLifecycleHandler {
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AnchorData data = player.getData(LOTMAnchors.ANCHOR_DATA);
        long now = System.currentTimeMillis();

        if (AnchorConfig.offlineDecayEnabled() && data.getLastSeenEpochMs() > 0) {
            long offlineMs = Math.max(0, now - data.getLastSeenEpochMs());
            long graceMs = AnchorConfig.offlineGraceHours() * 3_600_000L;
            if (offlineMs > graceMs && data.getTotalAnchors() > 0) {
                long interval = AnchorConfig.offlineDecayIntervalHours() * 3_600_000L;
                long periods = interval <= 0 ? 0 : (offlineMs - graceMs) / interval;
                if (periods > 0) {
                    double multiplier = Math.pow(1.0D - AnchorConfig.offlineDecayPercentPerDay() / 100.0D, periods);
                    long desired = (long) Math.floor(data.getTotalAnchors() * multiplier);
                    long currentAnchors = data.getTotalAnchors();
                    long lost = Math.round(currentAnchors * 0.20D);

                    data.removeAnchors(lost);
                    if (lost > 0) player.sendSystemMessage(Component.literal("§c[Anchors] You lost " + lost + " Anchors for prolonged absence."));
                }
            }
        }
        data.setLastSeenEpochMs(now);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(LOTMAnchors.ANCHOR_DATA).setLastSeenEpochMs(System.currentTimeMillis());
        }
    }
}

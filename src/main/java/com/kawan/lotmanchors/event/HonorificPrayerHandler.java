package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.config.AnchorConfig;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.UUID;

/**
 * Hooks after LOTMCraft's own honorific-name handler. A completed prayer is
 * represented by a new Pair(target UUID, praying player UUID) in answerState.
 */
public final class HonorificPrayerHandler {
    private int lastObservedSize = 0;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChat(ServerChatEvent event) {
        int size = HonorificNamesEventHandler.answerState.size();
        if (size <= lastObservedSize) {
            lastObservedSize = size;
            return;
        }

        for (int i = lastObservedSize; i < size; i++) {
            var pair = HonorificNamesEventHandler.answerState.get(i);
            UUID targetUuid = pair.getFirst();
            UUID worshipperUuid = pair.getSecond();
            ServerPlayer worshipper = event.getPlayer().server.getPlayerList().getPlayer(worshipperUuid);
            ServerPlayer target = event.getPlayer().server.getPlayerList().getPlayer(targetUuid);
            if (worshipper == null || target == null || worshipper.getUUID().equals(target.getUUID())) continue;

            String key = AnchorManager.key(worshipperUuid) + "->" + AnchorManager.key(targetUuid);
            var data = AnchorManager.get(target);
            long now = System.currentTimeMillis();
            if (data.isPrayerOnCooldown(key, now)) continue;

            data.setPrayerCooldown(key, now + AnchorConfig.prayerCooldownMinutes() * 60_000L);
            AnchorManager.give(target, AnchorConfig.prayerReward(), "Prayer for the Honorary Name");
            if (AnchorConfig.announcePrayerReward()) {
                worshipper.sendSystemMessage(Component.literal("§dYour prayer was acknowledged."));
            }
        }
        lastObservedSize = size;
    }
}

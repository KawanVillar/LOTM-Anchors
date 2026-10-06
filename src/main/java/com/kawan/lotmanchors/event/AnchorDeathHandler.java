package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.anchor.AnchorData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class AnchorDeathHandler {

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        AnchorData data = player.getData(LOTMAnchors.ANCHOR_DATA);

        long currentAnchors = data.getTotalAnchors();

        if (currentAnchors <= 0) {
            return;
        }

        long lost = Math.round(currentAnchors * 0.20D);

        if (lost <= 0) {
            return;
        }

        data.removeAnchors(lost);

        player.sendSystemMessage(Component.literal(
                "§c[Anchors] You lost " + lost
                        + " anchors upon dying."
        ));
    }
}
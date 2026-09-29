package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.anchor.AnchorManager;
import de.jakob.lotm.events.custom.StartAdvanceSequencePathwayEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;

/** Integrates with LOTMCraft's existing advancement failure mechanism. */
public final class AdvancementStabilityHandler {
    @SubscribeEvent
    public void onStartAdvance(StartAdvanceSequencePathwayEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;

        // LOTMCraft's event sequence is: create event -> post -> read failureChance -> roll.
        // We only add anchor-related risk; the main mod still performs the actual failure.
        double extraRisk = AnchorManager.failureChance(player, event.getSequence());
        if (extraRisk <= 0.0D) return;

        double combined = Math.max(event.getFailureChance(), extraRisk);
        event.setFailureChance(Math.min(1.0D, combined));

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§e[Âncoras] Estabilidade insuficiente para Seq. " + event.getSequence()
                        + " — risco de falha: " + Math.round(combined * 100.0D) + "%"
        ));
    }
}

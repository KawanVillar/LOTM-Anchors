package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.anchor.AnchorEntityIndex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public final class AnchorSourceCacheHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void joined(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level) AnchorEntityIndex.invalidate(level);
    }
    @SubscribeEvent public void left(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level) AnchorEntityIndex.invalidate(level);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void died(LivingDeathEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) AnchorEntityIndex.invalidate(level);
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) { player(event); }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event) { player(event); }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent event) { player(event); }
    @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        player(event);
        if (event.getEntity() instanceof ServerPlayer p) {
            ServerLevel previous = p.server.getLevel(event.getFrom());
            if (previous != null) AnchorEntityIndex.invalidate(previous);
        }
    }
    private static void player(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) AnchorEntityIndex.invalidate(p.serverLevel());
    }
    @SubscribeEvent public void stopped(ServerStoppedEvent event) { AnchorEntityIndex.clear(); }
}

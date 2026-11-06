package com.kawan.lotmanchors.event;
import com.kawan.lotmanchors.anchor.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.*;
public final class AnchorHistoryHandler {
    @SubscribeEvent public void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.tickCount % 100 != 0 || !p.isAlive()) return;
        Map<Integer, Long> totals = new HashMap<>();
        for (var row : AnchorSourceService.all(p)) if (row.category() != 0)
            totals.merge(row.category(), row.value(), Long::sum);
        var data = FollowerData.get(p.server);
        for (String change : data.history().observe(p.getUUID(), totals, p.server.getTickCount())) data.record(p.getUUID(), change);
    }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) FollowerData.get(p.server).history().forget(p.getUUID());
    }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) FollowerData.get(p.server).history().forget(p.getUUID());
    }
}

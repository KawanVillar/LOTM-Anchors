package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.anchor.AnchorSanityRules;
import com.kawan.lotmanchors.config.AnchorConfig;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.acting.ActingCapHelper;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AnchorSanityHandler {
    private final Map<UUID, AnchorSanityRules.Session> sessions = new HashMap<>();
    private final Map<UUID, AnchorSanityRules.Notices> notices = new HashMap<>();

    private static boolean applicable(ServerPlayer player, int sequence) {
        return sequence >= 0 && sequence <= 3 && BeyonderData.isBeyonder(player)
                && player.isAlive() && !player.isSpectator() && !player.isCreative();
    }

    public record View(float current, float ceiling, float nativeMaximum, long required, double deficit, String state) {}

    public static View view(ServerPlayer player, long anchors) {
        var sanity = player.getData(ModAttachments.SANITY_COMPONENT);
        float maximum = ActingCapHelper.getEffectiveCap(player);
        int sequence = BeyonderData.getSequence(player);
        long required = AnchorConfig.requiredAnchors(sequence);
        boolean configured = required > 0 && AnchorConfig.anchorSanityPenalty(sequence) > 0 && AnchorConfig.anchorSanityDropPerSecond() > 0;
        boolean applies = AnchorConfig.anchorSanityEnabled() && applicable(player, sequence) && configured;
        double deficit = applies ? AnchorSanityRules.deficit(anchors, required) : 0;
        float ceiling = applies ? AnchorSanityRules.ceiling(maximum, anchors, required, AnchorConfig.anchorSanityPenalty(sequence)) : maximum;
        String state = !AnchorConfig.anchorSanityEnabled() ? "Disabled"
                : !applicable(player, sequence) ? "Not applicable"
                : !configured ? "No pressure configured"
                : deficit <= 0 ? "Stable" : "Deficit (grace may apply)";
        return new View(sanity.getSanity(), ceiling, maximum, required, deficit, state);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        if (!AnchorConfig.anchorSanityEnabled()) { reset(player); return; }
        int sequence = BeyonderData.getSequence(player);
        if (!applicable(player, sequence)) {
            reset(player); return;
        }
        var session = sessions.computeIfAbsent(player.getUUID(), id -> new AnchorSanityRules.Session());
        int interval = AnchorConfig.anchorSanityIntervalSeconds();
        long tick = player.server.getTickCount();
        int graceTicks = AnchorConfig.anchorSanityGraceSeconds() * 20;
        int deficitGraceTicks = AnchorConfig.anchorSanityDeficitGraceSeconds() * 20;
        boolean ready = session.ready(tick, sequence, interval * 20, graceTicks);
        if (!ready && tick % (interval * 20) != 0) return;
        long anchors = AnchorManager.getAnchors(player);
        View state = view(player, anchors);
        boolean deficit = state.deficit() > 0;
        boolean pressureReady = false;
        if (!deficit) session.pressureReady(tick, false, 0);
        else if (ready) pressureReady = session.pressureReady(tick, true, deficitGraceTicks);
        long remaining = session.graceRemaining(tick, graceTicks)
                + session.deficitGraceRemaining(tick, deficitGraceTicks);
        String notice = notices.computeIfAbsent(player.getUUID(), id -> new AnchorSanityRules.Notices())
                .update(sequence, deficit, pressureReady ? 0 : remaining);
        if (AnchorConfig.anchorSanityWarnings() && !notice.isEmpty()) {
            String message = switch (notice) {
                case "recovered" -> "Anchor stability restored. Native sanity recovery is unchanged.";
                case "deficit" -> "Anchor deficit: " + anchors + "/" + state.required()
                        + ". Sanity pressure starts in about " + ((remaining + 19) / 20) + " seconds.";
                case "ending" -> "Anchor grace is ending: about " + ((remaining + 19) / 20)
                        + " seconds remaining. Restore anchors to prevent sanity pressure.";
                default -> "Anchor sanity pressure is active. Target ceiling: "
                        + Math.round(state.ceiling() * 100) + "%. Restore anchors to release this limit.";
            };
            player.sendSystemMessage(Component.literal("[Anchors] " + message));
        }
        if (!ready || !pressureReady || !deficit) return;
        float next = AnchorSanityRules.reduceToward(state.current(), state.ceiling(), state.nativeMaximum(),
                AnchorConfig.anchorSanityDropPerSecond(), interval);
        if (next < state.current()) player.getData(ModAttachments.SANITY_COMPONENT).setSanityAndSync(next, player);
    }

    private void reset(ServerPlayer player) {
        sessions.remove(player.getUUID()); notices.remove(player.getUUID());
    }
    @SubscribeEvent public void onLogin(PlayerEvent.PlayerLoggedInEvent event) { if (event.getEntity() instanceof ServerPlayer p) reset(p); }
    @SubscribeEvent public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { if (event.getEntity() instanceof ServerPlayer p) reset(p); }
    @SubscribeEvent public void onRespawn(PlayerEvent.PlayerRespawnEvent event) { if (event.getEntity() instanceof ServerPlayer p) reset(p); }
    @SubscribeEvent public void onStop(ServerStoppedEvent event) { sessions.clear(); notices.clear(); }
}

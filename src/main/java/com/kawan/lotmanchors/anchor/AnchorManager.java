package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.config.AnchorConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.UUID;

public final class AnchorManager {
    private AnchorManager() {}

    public static AnchorData get(ServerPlayer player) {
        return player.getData(LOTMAnchors.ANCHOR_DATA);
    }

    public static long getAnchors(ServerPlayer player) { return get(player).getTotalAnchors(); }

    public static void give(ServerPlayer player, long amount, String source) {
        if (amount <= 0) return;
        get(player).addAnchors(amount);
        player.sendSystemMessage(Component.literal("§a[Anchors] +" + amount + " §7(" + source + ")"));
    }

    public static long take(ServerPlayer player, long amount, String source) {
        long removed = get(player).removeAnchors(amount);
        if (removed > 0) player.sendSystemMessage(Component.literal("§c[Anchors] -" + removed + " §7(" + source + ")"));
        return removed;
    }

    public static void set(ServerPlayer player, long amount) {
        get(player).setAnchors(amount);
    }

    public static long requiredForNextAdvance(int currentSequence) {
        if (currentSequence < 1 || currentSequence > 4) return 0L;
        return AnchorConfig.requiredAnchors(currentSequence - 1);
    }

    public static long requiredForTargetSequence(int targetSequence) {
        if (targetSequence < 0 || targetSequence >= 4) return 0L;
        return AnchorConfig.requiredAnchors(targetSequence);
    }

    /**
     * LOTMCraft passes the target sequence to StartAdvanceSequencePathwayEvent.
     * Seq. 4 has no anchor requirement. From Seq. 3 onward, use the configured
     * requirement for the target sequence.
     */
    public static double failureChance(ServerPlayer player, int targetSequence) {
        if (targetSequence >= 4 || targetSequence < 0) return 0.0D;
        long required = requiredForTargetSequence(targetSequence);
        if (required <= 0) return 0.0D;

        long current = getAnchors(player);
        if (current >= required) return 0.0D;

        double deficitRatio = 1.0D - ((double) current / (double) required);
        double chance = deficitRatio * AnchorConfig.failureMultiplier();
        return Math.max(0.0D, Math.min(AnchorConfig.maxFailureChance(), chance));
    }

    public static String key(UUID uuid) { return uuid.toString().toLowerCase(Locale.ROOT); }
}

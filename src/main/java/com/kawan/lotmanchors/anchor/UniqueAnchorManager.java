package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.config.AnchorConfig;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;

/** Compatibility accessors backed by the same source rows used in the detailed panel. */
public final class UniqueAnchorManager {
    private UniqueAnchorManager() {}

    private static List<AnchorSourceRow> sources(ServerPlayer player, int category) {
        if (player.getServer() == null) return List.of();
        return category == 1 || category == 2 ? AnchorSourceService.subordinates(player) : AnchorSourceService.exclusive(player);
    }
    private static int[] counts(ServerPlayer player, int category, int size) {
        int[] counts = new int[size];
        for (var row : sources(player, category)) {
            boolean matches = category == 2 ? row.category() == 1 || row.category() == 2 : row.category() == category;
            if (matches && row.active() && row.sequence() >= 0 && row.sequence() < size) counts[row.sequence()]++;
        }
        return counts;
    }
    private static long points(int category, int[] counts) {
        long total = 0;
        for (int seq = 0; seq < counts.length; seq++) total += (long) counts[seq] * AnchorConfig.uniqueSourceAnchors(category, seq);
        return total;
    }
    private static int sum(int[] counts) { return java.util.Arrays.stream(counts).sum(); }

    public static int[] countErrorAvatarsBySequence(ServerPlayer player) { return counts(player, 3, 7); }
    public static long getErrorAvatarUniqueAnchors(ServerPlayer player) { return points(3, countErrorAvatarsBySequence(player)); }
    public static int countErrorAvatars(ServerPlayer player) { return sum(countErrorAvatarsBySequence(player)); }
    public static int[] countMarionettesBySequence(ServerPlayer player) { return counts(player, 4, 11); }
    public static long getMarionetteUniqueAnchors(ServerPlayer player) { return points(4, countMarionettesBySequence(player)); }
    public static int countMarionettes(ServerPlayer player) { return sum(countMarionettesBySequence(player)); }
    public static int[] countPersonasBySequence(ServerPlayer player) { return counts(player, 5, 6); }
    public static long getPersonaUniqueAnchors(ServerPlayer player) { return points(5, countPersonasBySequence(player)); }
    public static int countPersonas(ServerPlayer player) { return sum(countPersonasBySequence(player)); }
    public static int[] countSubordinatesBySequence(ServerPlayer player) { return counts(player, 2, 11); }
    public static int countSubordinates(ServerPlayer player) { return sum(countSubordinatesBySequence(player)); }
    public static long getSubordinateUniqueAnchors(ServerPlayer player) { return getSubordinateUniqueAnchors(countSubordinatesBySequence(player)); }
    public static long getSubordinateUniqueAnchors(int[] counts) { return points(2, counts); }
}
package com.kawan.lotmanchors.anchor;

/** Normal-anchor decay after grace, using only complete real-world intervals. */
public final class OfflineAnchorDecay {
    private OfflineAnchorDecay() {}
    public static long remaining(long anchors, long offlineMillis, int graceHours,
                                 double percentPerDay, int intervalHours) {
        if (anchors <= 0 || intervalHours <= 0 || percentPerDay <= 0) return Math.max(0, anchors);
        long eligibleMillis = offlineMillis - Math.max(0, graceHours) * 3_600_000L;
        if (eligibleMillis <= 0) return anchors;
        long periods = eligibleMillis / (intervalHours * 3_600_000L);
        if (periods == 0) return anchors;
        double days = periods * (intervalHours / 24.0);
        double retained = Math.pow(1 - Math.min(100, percentPerDay) / 100.0, days);
        return Math.max(0, Math.min(anchors, (long) Math.floor(anchors * retained)));
    }
}

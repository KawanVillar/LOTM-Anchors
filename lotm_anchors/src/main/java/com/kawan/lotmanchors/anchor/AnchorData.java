package com.kawan.lotmanchors.anchor;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

/** Persistent per-player anchor state. */
public final class AnchorData implements INBTSerializable<CompoundTag> {
    public static final String NBT_TOTAL = "total_anchors";
    public static final String NBT_LIFETIME_EARNED = "lifetime_earned";
    public static final String NBT_LIFETIME_LOST = "lifetime_lost";
    public static final String NBT_LAST_SEEN = "last_seen_epoch_ms";
    public static final String NBT_PRAYER_COOLDOWNS = "prayer_cooldowns";

    private long totalAnchors;
    private long lifetimeEarned;
    private long lifetimeLost;
    private long lastSeenEpochMs;
    private final Map<String, Long> prayerCooldowns = new LinkedHashMap<>();

    public long getTotalAnchors() { return totalAnchors; }
    public long getLifetimeEarned() { return lifetimeEarned; }
    public long getLifetimeLost() { return lifetimeLost; }
    public long getLastSeenEpochMs() { return lastSeenEpochMs; }
    public void setLastSeenEpochMs(long value) { lastSeenEpochMs = value; }

    public void addAnchors(long amount) {
        if (amount <= 0) return;
        totalAnchors = safeAdd(totalAnchors, amount);
        lifetimeEarned = safeAdd(lifetimeEarned, amount);
    }

    public long removeAnchors(long amount) {
        if (amount <= 0) return 0;
        long removed = Math.min(totalAnchors, amount);
        totalAnchors -= removed;
        lifetimeLost = safeAdd(lifetimeLost, removed);
        return removed;
    }

    public void setAnchors(long amount) {
        totalAnchors = Math.max(0, amount);
    }

    public boolean isPrayerOnCooldown(String key, long now) {
        Long until = prayerCooldowns.get(key);
        if (until == null || until <= now) {
            prayerCooldowns.remove(key);
            return false;
        }
        return true;
    }

    public void setPrayerCooldown(String key, long until) { prayerCooldowns.put(key, until); }

    private static long safeAdd(long a, long b) {
        if (Long.MAX_VALUE - a < b) return Long.MAX_VALUE;
        return a + b;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(NBT_TOTAL, totalAnchors);
        tag.putLong(NBT_LIFETIME_EARNED, lifetimeEarned);
        tag.putLong(NBT_LIFETIME_LOST, lifetimeLost);
        tag.putLong(NBT_LAST_SEEN, lastSeenEpochMs);
        CompoundTag cooldowns = new CompoundTag();
        prayerCooldowns.forEach((key, value) -> cooldowns.putLong(key, value));
        tag.put(NBT_PRAYER_COOLDOWNS, cooldowns);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        totalAnchors = Math.max(0, tag.getLong(NBT_TOTAL));
        lifetimeEarned = Math.max(0, tag.getLong(NBT_LIFETIME_EARNED));
        lifetimeLost = Math.max(0, tag.getLong(NBT_LIFETIME_LOST));
        lastSeenEpochMs = Math.max(0, tag.getLong(NBT_LAST_SEEN));
        prayerCooldowns.clear();
        if (tag.contains(NBT_PRAYER_COOLDOWNS)) {
            CompoundTag cooldowns = tag.getCompound(NBT_PRAYER_COOLDOWNS);
            for (String key : cooldowns.getAllKeys()) prayerCooldowns.put(key, cooldowns.getLong(key));
        }
    }
}

package com.kawan.lotmanchors.anchor;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Persistent per-player anchor state. */
public final class AnchorData implements INBTSerializable<CompoundTag> {

    public static final String NBT_TOTAL = "total_anchors";
    public static final String NBT_LIFETIME_EARNED = "lifetime_earned";
    public static final String NBT_LIFETIME_LOST = "lifetime_lost";
    public static final String NBT_LAST_SEEN = "last_seen_epoch_ms";
    public static final String NBT_PRAYER_COOLDOWNS = "prayer_cooldowns";
    public static final String NBT_REWARDED_QUESTS = "rewarded_quests";

    public static final String NBT_ACTIVE_QUEST = "active_quest";
    public static final String NBT_ACTIVE_QUEST_PROGRESS = "active_quest_progress";

    private long totalAnchors;
    private long lifetimeEarned;
    private long lifetimeLost;
    private long lastSeenEpochMs;

    private final Map<String, Long> prayerCooldowns = new LinkedHashMap<>();
    private final Set<String> rewardedQuests = new HashSet<>();

    private String activeQuest = "";
    private float activeQuestProgress = -1.0F;

    public long getTotalAnchors() { return totalAnchors; }
    public String getActiveQuest() { return activeQuest; }
    public float getActiveQuestProgress() { return activeQuestProgress; }
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

    public void setPrayerCooldown(String key, long until) {
        prayerCooldowns.put(key, until);
    }

    /*
     * =========================
     * Quest Rewards
     * =========================
     */

    public boolean hasQuestReward(String questId) {
        return rewardedQuests.contains(questId);
    }

    public void markQuestRewarded(String questId) {
        rewardedQuests.add(questId);
    }

    /*
     * =========================
     * Utility
     * =========================
     */

    private static long safeAdd(long a, long b) {
        if (Long.MAX_VALUE - a < b) {
            return Long.MAX_VALUE;
        }

        return a + b;
    }

    /*
     * =========================
     * NBT Serialization
     * =========================
     */

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putLong(NBT_TOTAL, totalAnchors);
        tag.putLong(NBT_LIFETIME_EARNED, lifetimeEarned);
        tag.putLong(NBT_LIFETIME_LOST, lifetimeLost);
        tag.putLong(NBT_LAST_SEEN, lastSeenEpochMs);

        tag.putString(NBT_ACTIVE_QUEST, activeQuest);
        tag.putFloat(NBT_ACTIVE_QUEST_PROGRESS, activeQuestProgress);

        // Prayer cooldowns
        CompoundTag cooldowns = new CompoundTag();

        prayerCooldowns.forEach((key, value) ->
                cooldowns.putLong(key, value)
        );

        tag.put(NBT_PRAYER_COOLDOWNS, cooldowns);

        // Quests that already gave an Anchor reward
        CompoundTag rewarded = new CompoundTag();

        for (String questId : rewardedQuests) {
            rewarded.putBoolean(questId, true);
        }

        tag.put(NBT_REWARDED_QUESTS, rewarded);

        return tag;
    }

    @Override
    public void deserializeNBT(
            HolderLookup.Provider provider,
            CompoundTag tag
    ) {
        totalAnchors = Math.max( 0, tag.getLong(NBT_TOTAL));
        lifetimeEarned = Math.max( 0, tag.getLong(NBT_LIFETIME_EARNED));
        lifetimeLost = Math.max( 0, tag.getLong(NBT_LIFETIME_LOST));
        lastSeenEpochMs = Math.max( 0, tag.getLong(NBT_LAST_SEEN));
        activeQuest = tag.getString(NBT_ACTIVE_QUEST);

        activeQuestProgress = tag.contains(NBT_ACTIVE_QUEST_PROGRESS)
                ? tag.getFloat(NBT_ACTIVE_QUEST_PROGRESS)
                : -1.0F;
        /*
         * =========================
         * Prayer Cooldowns
         * =========================
         */

        prayerCooldowns.clear();

        if (tag.contains(NBT_PRAYER_COOLDOWNS)) {
            CompoundTag cooldowns =
                    tag.getCompound(NBT_PRAYER_COOLDOWNS);

            for (String key : cooldowns.getAllKeys()) {
                prayerCooldowns.put(
                        key,
                        cooldowns.getLong(key)
                );
            }
        }

        /*
         * =========================
         * Rewarded Quests
         * =========================
         */

        rewardedQuests.clear();

        if (tag.contains(NBT_REWARDED_QUESTS)) {
            CompoundTag rewarded =
                    tag.getCompound(NBT_REWARDED_QUESTS);

            for (String questId : rewarded.getAllKeys()) {
                if (rewarded.getBoolean(questId)) {
                    rewardedQuests.add(questId);
                }
            }
        }
    }
    public void setActiveQuest(String questId, float progress) {
        activeQuest = questId == null ? "" : questId;
        activeQuestProgress = progress;
    }

    public void clearActiveQuest() {
        activeQuest = "";
        activeQuestProgress = -1.0F;
    }
}
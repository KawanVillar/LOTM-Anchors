package com.kawan.lotmanchors.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class AnchorConfig {
    private static ModConfigSpec.IntValue SEQ3;
    private static ModConfigSpec.IntValue SEQ2;
    private static ModConfigSpec.IntValue SEQ1;
    private static ModConfigSpec.IntValue SEQ0;
    private static ModConfigSpec.DoubleValue FAILURE_MULTIPLIER;
    private static ModConfigSpec.DoubleValue MAX_FAILURE;
    private static ModConfigSpec.IntValue PRAYER_REWARD;
    private static ModConfigSpec.IntValue PRAYER_COOLDOWN_MINUTES;
    private static ModConfigSpec.BooleanValue OFFLINE_DECAY;
    private static ModConfigSpec.IntValue OFFLINE_GRACE_HOURS;
    private static ModConfigSpec.DoubleValue OFFLINE_DECAY_PERCENT_PER_DAY;
    private static ModConfigSpec.IntValue OFFLINE_DECAY_INTERVAL_HOURS;
    private static ModConfigSpec.BooleanValue ANNOUNCE_PRAYER_REWARD;

    private AnchorConfig() {}

    public static void register(ModContainer container) {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("LOTM Anchors - provisional server rules. Review these values during development.").push("anchors");
        SEQ3 = b.comment("Anchors required to advance TO Sequence 3.").defineInRange("required_sequence_3", 1000, 0, Integer.MAX_VALUE);
        SEQ2 = b.comment("Anchors required to advance TO Sequence 2.").defineInRange("required_sequence_2", 3000, 0, Integer.MAX_VALUE);
        SEQ1 = b.comment("Anchors required to advance TO Sequence 1.").defineInRange("required_sequence_1", 10000, 0, Integer.MAX_VALUE);
        SEQ0 = b.comment("Anchors required to advance TO Sequence 0.").defineInRange("required_sequence_0", 30000, 0, Integer.MAX_VALUE);
        FAILURE_MULTIPLIER = b.comment("1.0 = missing-anchor ratio becomes failure chance; 0.5 = half that chance.").defineInRange("failure_multiplier", 1.0D, 0.0D, 1.0D);
        MAX_FAILURE = b.comment("Hard cap for anchor-related failure chance. LOTMCraft still handles the actual failure/regression/death sequence.").defineInRange("max_failure_chance", 1.0D, 0.0D, 1.0D);
        PRAYER_REWARD = b.comment("Anchors awarded when a valid honorific-name prayer is completed.").defineInRange("prayer_reward", 1, 0, Integer.MAX_VALUE);
        PRAYER_COOLDOWN_MINUTES = b.comment("Per worshipper -> target cooldown to prevent anchor farming by chat spam.").defineInRange("prayer_cooldown_minutes", 60, 0, Integer.MAX_VALUE);
        ANNOUNCE_PRAYER_REWARD = b.define("announce_prayer_reward", true);
        b.pop();

        b.comment("Optional offline anchor decay. Disabled by default until the server rules are finalized.").push("offline_decay");
        OFFLINE_DECAY = b.define("enabled", false);
        OFFLINE_GRACE_HOURS = b.defineInRange("grace_hours", 72, 0, Integer.MAX_VALUE);
        OFFLINE_DECAY_PERCENT_PER_DAY = b.defineInRange("percent_per_day", 2.0D, 0.0D, 100.0D);
        OFFLINE_DECAY_INTERVAL_HOURS = b.defineInRange("interval_hours", 24, 1, Integer.MAX_VALUE);
        b.pop();

        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, b.build(), "lotm_anchors-server.toml");
    }

    public static long requiredAnchors(int targetSequence) {
        return switch (targetSequence) {
            case 3 -> SEQ3.get();
            case 2 -> SEQ2.get();
            case 1 -> SEQ1.get();
            case 0 -> SEQ0.get();
            default -> 0L;
        };
    }
    public static double failureMultiplier() { return FAILURE_MULTIPLIER.get(); }
    public static double maxFailureChance() { return MAX_FAILURE.get(); }
    public static int prayerReward() { return PRAYER_REWARD.get(); }
    public static int prayerCooldownMinutes() { return PRAYER_COOLDOWN_MINUTES.get(); }
    public static boolean announcePrayerReward() { return ANNOUNCE_PRAYER_REWARD.get(); }
    public static boolean offlineDecayEnabled() { return OFFLINE_DECAY.get(); }
    public static int offlineGraceHours() { return OFFLINE_GRACE_HOURS.get(); }
    public static double offlineDecayPercentPerDay() { return OFFLINE_DECAY_PERCENT_PER_DAY.get(); }
    public static int offlineDecayIntervalHours() { return OFFLINE_DECAY_INTERVAL_HOURS.get(); }
}

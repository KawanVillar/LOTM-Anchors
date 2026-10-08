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
    private static ModConfigSpec.DoubleValue QUEST_REPEAT_MULTIPLIER;
    private static ModConfigSpec.IntValue PRAYER_COOLDOWN_MINUTES;
    private static ModConfigSpec.BooleanValue OFFLINE_DECAY;
    private static ModConfigSpec.IntValue OFFLINE_GRACE_HOURS;
    private static ModConfigSpec.DoubleValue OFFLINE_DECAY_PERCENT_PER_DAY;
    private static ModConfigSpec.IntValue OFFLINE_DECAY_INTERVAL_HOURS;
    private static ModConfigSpec.BooleanValue ANNOUNCE_PRAYER_REWARD;

    // Error Avatar Unique Anchors
    private static ModConfigSpec.IntValue ERROR_AVATAR_SEQ1;
    private static ModConfigSpec.IntValue ERROR_AVATAR_SEQ2;
    private static ModConfigSpec.IntValue ERROR_AVATAR_SEQ3;
    private static ModConfigSpec.IntValue ERROR_AVATAR_SEQ4;
    private static ModConfigSpec.IntValue ERROR_AVATAR_SEQ5;
    private static ModConfigSpec.IntValue ERROR_AVATAR_SEQ6;

    // Marionette Unique Anchors
    private static ModConfigSpec.IntValue MARIONETTE_SEQ1;
    private static ModConfigSpec.IntValue MARIONETTE_SEQ2;
    private static ModConfigSpec.IntValue MARIONETTE_SEQ3;
    private static ModConfigSpec.IntValue MARIONETTE_SEQ4;
    private static ModConfigSpec.IntValue MARIONETTE_SEQ5;
    private static ModConfigSpec.IntValue MARIONETTE_SEQ6;
    private static ModConfigSpec.IntValue MARIONETTE_SEQ7;
    private static ModConfigSpec.IntValue MARIONETTE_SEQ8;
    private static ModConfigSpec.IntValue MARIONETTE_SEQ9;
    private static ModConfigSpec.IntValue MARIONETTE_NON_BEYONDER;

    // Persona Unique Anchors
    private static ModConfigSpec.IntValue PERSONA_SEQ1;
    private static ModConfigSpec.IntValue PERSONA_SEQ2;
    private static ModConfigSpec.IntValue PERSONA_SEQ3;
    private static ModConfigSpec.IntValue PERSONA_SEQ4;
    private static ModConfigSpec.IntValue PERSONA_SEQ5;

    // Quest Reward
    private static ModConfigSpec.IntValue QUEST_KILL_ZOMBIES;
    private static ModConfigSpec.IntValue QUEST_DEFEND_VILLAGE;
    private static ModConfigSpec.IntValue QUEST_KILL_TYRANT_SEQ4;
    private static ModConfigSpec.IntValue QUEST_KILL_SEQ4;
    private static ModConfigSpec.IntValue QUEST_KILL_SEQ3;
    private static ModConfigSpec.IntValue QUEST_KILL_SEQ2;
    private static ModConfigSpec.IntValue QUEST_KILL_SEQ1;
    private static ModConfigSpec.IntValue QUEST_COLLECT_LOW_SEQ_CHARACTERISTICS;
    private static ModConfigSpec.IntValue QUEST_FIND_RANDOM_STRUCTURES;
    private static ModConfigSpec.IntValue QUEST_KILL_PLAYER_TARGET;
    private static ModConfigSpec.IntValue QUEST_HELP_BEYONDER;
    private static ModConfigSpec.IntValue QUEST_DELIVER_ITEM;

    private AnchorConfig() {}

    public static void register(ModContainer container) {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        // Main anchor settings
        b.comment("LOTM Anchors - provisional server rules. Review these values during development.")
                .push("anchors");

        SEQ3 = b.comment("Anchors required to advance TO Sequence 3.")
                .defineInRange("required_sequence_3", 1000, 0, Integer.MAX_VALUE);

        SEQ2 = b.comment("Anchors required to advance TO Sequence 2.")
                .defineInRange("required_sequence_2", 3000, 0, Integer.MAX_VALUE);

        SEQ1 = b.comment("Anchors required to advance TO Sequence 1.")
                .defineInRange("required_sequence_1", 10000, 0, Integer.MAX_VALUE);

        SEQ0 = b.comment("Anchors required to advance TO Sequence 0.")
                .defineInRange("required_sequence_0", 30000, 0, Integer.MAX_VALUE);

        FAILURE_MULTIPLIER = b.comment(
                        "1.0 = missing-anchor ratio becomes failure chance; 0.5 = half that chance."
                )
                .defineInRange("failure_multiplier", 1.0D, 0.0D, 1.0D);

        MAX_FAILURE = b.comment(
                        "Hard cap for anchor-related failure chance. LOTMCraft still handles the actual failure/regression/death sequence."
                )
                .defineInRange("max_failure_chance", 1.0D, 0.0D, 1.0D);

        PRAYER_REWARD = b.comment(
                        "Anchors awarded when a valid honorific-name prayer is completed."
                )
                .defineInRange("prayer_reward", 1, 0, Integer.MAX_VALUE);

        QUEST_REPEAT_MULTIPLIER = b.comment(
                        "Multiplier applied to a LOTMCraft quest after its first completion."
                )
                .defineInRange("quest_repeat_multiplier", 0.60D, 0.0D, 1.0D);

        PRAYER_COOLDOWN_MINUTES = b.comment(
                        "Per worshipper -> target cooldown to prevent anchor farming by chat spam."
                )
                .defineInRange("prayer_cooldown_minutes", 60, 0, Integer.MAX_VALUE);

        ANNOUNCE_PRAYER_REWARD = b.define("announce_prayer_reward", true);

        b.pop();


        // Error Avatar Unique Anchors
        b.comment("Unique Anchors generated by Error pathway Avatars.")
                .push("unique_anchors.error_avatars");

        ERROR_AVATAR_SEQ1 = b.comment(
                        "Unique Anchors generated by one Sequence 1 Error Avatar."
                )
                .defineInRange("sequence_1", 10_000, 0, Integer.MAX_VALUE);

        ERROR_AVATAR_SEQ2 = b.comment(
                        "Unique Anchors generated by one Sequence 2 Error Avatar."
                )
                .defineInRange("sequence_2", 2_500, 0, Integer.MAX_VALUE);

        ERROR_AVATAR_SEQ3 = b.comment(
                        "Unique Anchors generated by one Sequence 3 Error Avatar."
                )
                .defineInRange("sequence_3", 500, 0, Integer.MAX_VALUE);

        ERROR_AVATAR_SEQ4 = b.comment(
                        "Unique Anchors generated by one Sequence 4 Error Avatar."
                )
                .defineInRange("sequence_4", 100, 0, Integer.MAX_VALUE);

        ERROR_AVATAR_SEQ5 = b.comment(
                        "Unique Anchors generated by one Sequence 5 Error Avatar."
                )
                .defineInRange("sequence_5", 50, 0, Integer.MAX_VALUE);

        ERROR_AVATAR_SEQ6 = b.comment(
                        "Unique Anchors generated by one Sequence 6 Error Avatar."
                )
                .defineInRange("sequence_6", 25, 0, Integer.MAX_VALUE);

        b.pop();


        // Marionette Unique Anchors
        b.comment("Unique Anchors generated by Marionettes.")
                .push("unique_anchors.marionettes");

        MARIONETTE_SEQ1 = b.comment(
                        "Unique Anchors generated by one Sequence 1 Marionette."
                )
                .defineInRange("sequence_1", 10_000, 0, Integer.MAX_VALUE);

        MARIONETTE_SEQ2 = b.comment(
                        "Unique Anchors generated by one Sequence 2 Marionette."
                )
                .defineInRange("sequence_2", 2_500, 0, Integer.MAX_VALUE);

        MARIONETTE_SEQ3 = b.comment(
                        "Unique Anchors generated by one Sequence 3 Marionette."
                )
                .defineInRange("sequence_3", 500, 0, Integer.MAX_VALUE);

        MARIONETTE_SEQ4 = b.comment(
                        "Unique Anchors generated by one Sequence 4 Marionette."
                )
                .defineInRange("sequence_4", 100, 0, Integer.MAX_VALUE);

        MARIONETTE_SEQ5 = b.comment(
                        "Unique Anchors generated by one Sequence 5 Marionette."
                )
                .defineInRange("sequence_5", 50, 0, Integer.MAX_VALUE);

        MARIONETTE_SEQ6 = b.comment(
                        "Unique Anchors generated by one Sequence 6 Marionette."
                )
                .defineInRange("sequence_6", 25, 0, Integer.MAX_VALUE);

        MARIONETTE_SEQ7 = b.comment(
                        "Unique Anchors generated by one Sequence 7 Marionette."
                )
                .defineInRange("sequence_7", 15, 0, Integer.MAX_VALUE);

        MARIONETTE_SEQ8 = b.comment(
                        "Unique Anchors generated by one Sequence 8 Marionette."
                )
                .defineInRange("sequence_8", 10, 0, Integer.MAX_VALUE);

        MARIONETTE_SEQ9 = b.comment(
                        "Unique Anchors generated by one Sequence 9 Marionette."
                )
                .defineInRange("sequence_9", 10, 0, Integer.MAX_VALUE);

        MARIONETTE_NON_BEYONDER = b.comment(
                        "Unique Anchors generated by one Non-Beyonder Marionette."
                )
                .defineInRange("non_beyonder", 10, 0, Integer.MAX_VALUE);

        b.pop();


        // Persona Unique Anchors
        b.comment("Unique Anchors generated by Spectator pathway Personas.")
                .push("unique_anchors.personas");

        PERSONA_SEQ1 = b.comment(
                        "Unique Anchors generated by one Sequence 1 Persona."
                )
                .defineInRange("sequence_1", 10_000, 0, Integer.MAX_VALUE);

        PERSONA_SEQ2 = b.comment(
                        "Unique Anchors generated by one Sequence 2 Persona."
                )
                .defineInRange("sequence_2", 2_500, 0, Integer.MAX_VALUE);

        PERSONA_SEQ3 = b.comment(
                        "Unique Anchors generated by one Sequence 3 Persona."
                )
                .defineInRange("sequence_3", 500, 0, Integer.MAX_VALUE);

        PERSONA_SEQ4 = b.comment(
                        "Unique Anchors generated by one Sequence 4 Persona."
                )
                .defineInRange("sequence_4", 100, 0, Integer.MAX_VALUE);

        PERSONA_SEQ5 = b.comment(
                        "Unique Anchors generated by one Sequence 5 Persona."
                )
                .defineInRange("sequence_5", 50, 0, Integer.MAX_VALUE);

        b.pop();


        // Quest Rewards
        b.comment("Quest Anchor rewards.")
                .push("quests");

        QUEST_KILL_ZOMBIES = b.comment(
                        "Anchors awarded for the first completion of Kill Zombies."
                )
                .defineInRange("kill_zombies", 25, 0, Integer.MAX_VALUE);

        QUEST_DEFEND_VILLAGE = b.comment(
                        "Anchors awarded for Defend Village. This quest always gives the full reward."
                )
                .defineInRange("defend_village", 100, 0, Integer.MAX_VALUE);

        QUEST_KILL_TYRANT_SEQ4 = b.comment(
                        "Anchors awarded for the first completion of Kill Tyrant Seq 4."
                )
                .defineInRange("kill_tyrant_seq4", 250, 0, Integer.MAX_VALUE);

        QUEST_KILL_SEQ4 = b.comment(
                        "Anchors awarded for the first completion of Kill Seq 4."
                )
                .defineInRange("kill_seq4", 250, 0, Integer.MAX_VALUE);

        QUEST_KILL_SEQ3 = b.comment(
                        "Anchors awarded for the first completion of Kill Seq 3."
                )
                .defineInRange("kill_seq3", 500, 0, Integer.MAX_VALUE);

        QUEST_KILL_SEQ2 = b.comment(
                        "Anchors awarded for the first completion of Kill Seq 2."
                )
                .defineInRange("kill_seq2", 1000, 0, Integer.MAX_VALUE);

        QUEST_KILL_SEQ1 = b.comment(
                        "Anchors awarded for the first completion of Kill Seq 1."
                )
                .defineInRange("kill_seq1", 2000, 0, Integer.MAX_VALUE);

        QUEST_COLLECT_LOW_SEQ_CHARACTERISTICS = b.comment(
                        "Anchors awarded for the first completion of Collect Low Sequence Characteristics."
                )
                .defineInRange("collect_low_seq_characteristics", 200, 0, Integer.MAX_VALUE);

        QUEST_FIND_RANDOM_STRUCTURES = b.comment(
                        "Anchors awarded for the first completion of Find Random Structures."
                )
                .defineInRange("find_random_structures", 100, 0, Integer.MAX_VALUE);

        QUEST_KILL_PLAYER_TARGET = b.comment(
                        "Anchors awarded for the first completion of Kill Player Target."
                )
                .defineInRange("kill_player_target", 300, 0, Integer.MAX_VALUE);

        QUEST_HELP_BEYONDER = b.comment(
                        "Anchors awarded for the first completion of Help Beyonder."
                )
                .defineInRange("help_beyonder", 150, 0, Integer.MAX_VALUE);

        QUEST_DELIVER_ITEM = b.comment(
                        "Anchors awarded for the first completion of Deliver Item."
                )
                .defineInRange("deliver_item", 50, 0, Integer.MAX_VALUE);

        b.pop();


        // Optional offline decay
        b.comment(
                        "Optional offline anchor decay. Disabled by default until the server rules are finalized."
                )
                .push("offline_decay");

        OFFLINE_DECAY = b.define("enabled", false);

        OFFLINE_GRACE_HOURS = b.defineInRange(
                "grace_hours",
                72,
                0,
                Integer.MAX_VALUE
        );

        OFFLINE_DECAY_PERCENT_PER_DAY = b.defineInRange(
                "percent_per_day",
                2.0D,
                0.0D,
                100.0D
        );

        OFFLINE_DECAY_INTERVAL_HOURS = b.defineInRange(
                "interval_hours",
                24,
                1,
                Integer.MAX_VALUE
        );

        b.pop();

        container.registerConfig(
                net.neoforged.fml.config.ModConfig.Type.SERVER,
                b.build(),
                "lotm_anchors-server.toml"
        );
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

    public static double failureMultiplier() {
        return FAILURE_MULTIPLIER.get();
    }

    public static double maxFailureChance() {
        return MAX_FAILURE.get();
    }

    public static int prayerReward() {
        return PRAYER_REWARD.get();
    }

    public static double questRepeatMultiplier() {
        return QUEST_REPEAT_MULTIPLIER.get();
    }

    public static int prayerCooldownMinutes() {
        return PRAYER_COOLDOWN_MINUTES.get();
    }

    public static boolean announcePrayerReward() {
        return ANNOUNCE_PRAYER_REWARD.get();
    }

    public static boolean offlineDecayEnabled() {
        return OFFLINE_DECAY.get();
    }

    public static int offlineGraceHours() {
        return OFFLINE_GRACE_HOURS.get();
    }

    public static double offlineDecayPercentPerDay() {
        return OFFLINE_DECAY_PERCENT_PER_DAY.get();
    }

    public static int offlineDecayIntervalHours() {
        return OFFLINE_DECAY_INTERVAL_HOURS.get();
    }

    // Error Avatar values
    public static long errorAvatarSequence1Anchors() {
        return ERROR_AVATAR_SEQ1.get();
    }

    public static long errorAvatarSequence2Anchors() {
        return ERROR_AVATAR_SEQ2.get();
    }

    public static long errorAvatarSequence3Anchors() {
        return ERROR_AVATAR_SEQ3.get();
    }

    public static long errorAvatarSequence4Anchors() {
        return ERROR_AVATAR_SEQ4.get();
    }

    public static long errorAvatarSequence5Anchors() {
        return ERROR_AVATAR_SEQ5.get();
    }

    public static long errorAvatarSequence6Anchors() {
        return ERROR_AVATAR_SEQ6.get();
    }

    // Marionette values
    public static long marionetteSequence1Anchors() {
        return MARIONETTE_SEQ1.get();
    }

    public static long marionetteSequence2Anchors() {
        return MARIONETTE_SEQ2.get();
    }

    public static long marionetteSequence3Anchors() {
        return MARIONETTE_SEQ3.get();
    }

    public static long marionetteSequence4Anchors() {
        return MARIONETTE_SEQ4.get();
    }

    public static long marionetteSequence5Anchors() {
        return MARIONETTE_SEQ5.get();
    }

    public static long marionetteSequence6Anchors() {
        return MARIONETTE_SEQ6.get();
    }

    public static long marionetteSequence7Anchors() {
        return MARIONETTE_SEQ7.get();
    }

    public static long marionetteSequence8Anchors() {
        return MARIONETTE_SEQ8.get();
    }

    public static long marionetteSequence9Anchors() {
        return MARIONETTE_SEQ9.get();
    }

    public static long marionetteNonBeyonderAnchors() {
        return MARIONETTE_NON_BEYONDER.get();
    }

    // Persona values
    public static long personaSequence1Anchors() { return PERSONA_SEQ1.get(); }

    public static long personaSequence2Anchors() { return PERSONA_SEQ2.get(); }

    public static long personaSequence3Anchors() { return PERSONA_SEQ3.get(); }

    public static long personaSequence4Anchors() { return PERSONA_SEQ4.get(); }

    public static long personaSequence5Anchors() { return PERSONA_SEQ5.get(); }

    public static long questReward(String questId) {
        return switch (questId) {
            case "kill_zombies" -> QUEST_KILL_ZOMBIES.get();
            case "defend_village" -> QUEST_DEFEND_VILLAGE.get();
            case "kill_tyrant_seq4" -> QUEST_KILL_TYRANT_SEQ4.get();
            case "kill_seq4" -> QUEST_KILL_SEQ4.get();
            case "kill_seq3" -> QUEST_KILL_SEQ3.get();
            case "kill_seq2" -> QUEST_KILL_SEQ2.get();
            case "kill_seq1" -> QUEST_KILL_SEQ1.get();
            case "collect_low_seq_characteristics" -> QUEST_COLLECT_LOW_SEQ_CHARACTERISTICS.get();
            case "find_random_structures" -> QUEST_FIND_RANDOM_STRUCTURES.get();
            case "kill_player_target" -> QUEST_KILL_PLAYER_TARGET.get();
            case "help_beyonder" -> QUEST_HELP_BEYONDER.get();
            case "deliver_item" -> QUEST_DELIVER_ITEM.get();
            default -> 0L;
        };
    }
}
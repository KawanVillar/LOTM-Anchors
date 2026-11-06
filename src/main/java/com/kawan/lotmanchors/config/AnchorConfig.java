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
    private static ModConfigSpec.BooleanValue ANCHOR_SANITY_ENABLED, ANCHOR_SANITY_WARNINGS;
    private static ModConfigSpec.IntValue ANCHOR_SANITY_INTERVAL, ANCHOR_SANITY_GRACE, ANCHOR_SANITY_DEFICIT_GRACE;
    private static ModConfigSpec.DoubleValue ANCHOR_SANITY_DROP;
    private static final ModConfigSpec.DoubleValue[] ANCHOR_SANITY_PENALTIES = new ModConfigSpec.DoubleValue[4];
    private static final ModConfigSpec.IntValue[] FOLLOWER_SEQUENCE_ANCHORS = new ModConfigSpec.IntValue[11];
    private static ModConfigSpec.IntValue FOLLOWER_INVITE_SECONDS;
    private static ModConfigSpec.IntValue FOLLOWER_OFFER_COOLDOWN_SECONDS;
    private static ModConfigSpec.DoubleValue QUEST_REPEAT_MULTIPLIER;
    private static ModConfigSpec.BooleanValue OFFLINE_DECAY;
    private static ModConfigSpec.IntValue OFFLINE_GRACE_HOURS;
    private static ModConfigSpec.DoubleValue OFFLINE_DECAY_PERCENT_PER_DAY;
    private static ModConfigSpec.IntValue OFFLINE_DECAY_INTERVAL_HOURS;

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

    // Subordinate Unique Anchors
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ1;
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ2;
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ3;
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ4;
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ5;
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ6;
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ7;
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ8;
    private static ModConfigSpec.IntValue SUBORDINATE_SEQ9;
    private static ModConfigSpec.IntValue SUBORDINATE_NON_BEYONDER;

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
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,
                createSpec(), "lotm_anchors-server.toml");
    }

    public static ModConfigSpec createSpec() {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        // Main anchor settings
        b.comment("============================================================",
                        "LOTM ANCHORS | GLOBAL SERVER RULES",
                        "============================================================",
                        "One configuration per world; the server controls all players.",
                        "Advancement requirements are shared by every pathway.",
                        "Source contributions are configured separately below.",
                        "Counts and points are not percentages. Setting a requirement to 0 disables it.",
                        "All values are provisional gameplay balance, not canon measurements.")
                .push("anchors");

        SEQ0 = b.comment("ADVANCEMENT REQUIREMENTS | Target Sequence, strongest first.",
                        "Total anchor points required to advance TO Sequence 0.")
                .defineInRange("required_sequence_0", 30000, 0, Integer.MAX_VALUE);
        SEQ1 = b.comment("Total anchor points required to advance TO Sequence 1.")
                .defineInRange("required_sequence_1", 10000, 0, Integer.MAX_VALUE);
        SEQ2 = b.comment("Total anchor points required to advance TO Sequence 2.")
                .defineInRange("required_sequence_2", 3000, 0, Integer.MAX_VALUE);
        SEQ3 = b.comment("Total anchor points required to advance TO Sequence 3.",
                        "This addon does not require anchors for advancement TO Sequence 4 or lower ranks.")
                .defineInRange("required_sequence_3", 1000, 0, Integer.MAX_VALUE);

        FAILURE_MULTIPLIER = b.comment(
                        "ADVANCEMENT RISK | Fractions from 0.0 to 1.0, not 0 to 100.",
                        "Extra risk = missing-anchor ratio * this multiplier, limited by max_failure_chance.",
                        "1.0 = full deficit as risk; 0.5 = half; 0.0 = no anchor-related risk."
                )
                .defineInRange("failure_multiplier", 1.0D, 0.0D, 1.0D);

        MAX_FAILURE = b.comment(
                        "Maximum extra risk from missing anchors: 1.0 = 100%, 0.25 = 25%.",
                        "This does not replace LOTMCraft's own risk, ritual, or death mechanics."
                )
                .defineInRange("max_failure_chance", 1.0D, 0.0D, 1.0D);

        FOLLOWER_INVITE_SECONDS = b.comment("FAITH INVITATIONS | Timers in real-world seconds.",
                        "Expiry for each step: recipient's invitation option, then worshipper's consent prompt.",
                        "A prayer alone never grants points. Only an accepted faith bond contributes.")
                .defineInRange("follower_invite_seconds", 120, 10, 3600);
        FOLLOWER_OFFER_COOLDOWN_SECONDS = b.comment("Minimum seconds between invitation offers for the same worshipper and recipient.",
                        "Does not multiply or refresh the follower's contribution.")
                .defineInRange("follower_offer_cooldown_seconds", 60, 10, 3600);

        QUEST_REPEAT_MULTIPLIER = b.comment(
                        "QUEST REWARD POLICY | Fraction from 0.0 to 1.0.",
                        "Repeated quest completions grant base reward * this value, rounded to whole points.",
                        "0.60 = 60% of the first-completion reward. Defend Village always grants its full reward.",
                        "Individual base rewards are in [quests]."
                )
                .defineInRange("quest_repeat_multiplier", 0.60D, 0.0D, 1.0D);

        b.pop();

        b.comment("============================================================",
                        "FAITH | POINTS PER CONSENTING FOLLOWER",
                        "============================================================",
                        "Applies to every pathway. The Sequence below belongs to the FOLLOWER, not the recipient.",
                        "Values are added once per faith bond, not once per prayer.",
                        "Advancement and regression update the value. Offline followers use their last known Sequence.",
                        "Set an entry to 0 to disable the contribution of that Sequence.",
                        "Sequence 0 is strongest; non_beyonder is an ordinary person.")
                .push("follower_values");
        int[] followerDefaults = {30_000, 10_000, 2_500, 500, 100, 50, 35, 25, 15, 10, 5};
        for (int sequence = 0; sequence <= 10; sequence++) {
            FOLLOWER_SEQUENCE_ANCHORS[sequence] = b.comment(sequence == 10
                            ? "Points per consenting non-Beyonder follower."
                            : "Points per consenting Sequence " + sequence + " follower.").defineInRange(
                    sequence == 10 ? "non_beyonder" : "sequence_" + sequence,
                    followerDefaults[sequence], 0, Integer.MAX_VALUE);
        }
        b.pop();

        b.comment("============================================================",
                        "PATHWAY SOURCES | POINTS PER VALID LINKED ENTITY",
                        "============================================================",
                        "Only the owning pathway receives these contributions.",
                        "These values add to normal anchors and faith bonds; they are not deposited permanently.",
                        "Sequence keys refer to the linked entity's Sequence, strongest first.",
                        "Setting an entry to 0 disables its contribution. Unsupported Sequences are not counted.")
                .push("unique_anchors");

        // Error Avatar Unique Anchors
        b.comment("ERROR | AVATARS", "Owner must be on the Error pathway.",
                        "Only loaded avatars linked to their original owner count. Supported Sequences: 1 to 6.")
                .push("error_avatars");

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
        b.comment("FOOL | MARIONETTES", "Owner must be on the Fool pathway.",
                        "Only loaded entities with a valid native marionette/controller link count.",
                        "Supported Sequences: 1 to 9, plus non-Beyonders.")
                .push("marionettes");

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
        b.comment("VISIONARY | VIRTUAL PERSONAS", "Owner must be on the Visionary pathway.",
                        "Uses native Persona data on online players and loaded linked Persona avatars.",
                        "Supported Sequences: 1 to 5.")
                .push("personas");

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

        // Subordinate Unique Anchors
        b.comment("RED PRIEST | TEAM PLAYERS AND NPC SUBORDINATES", "Owner must remain on the Red Priest pathway.",
                        "This shared table applies to both Team players and NPC subordinates.",
                        "Team players must be online, alive, not spectating, and confirm the leader's native roster.",
                        "Team leadership requires Sequence 3 or stronger. NPCs remain eligible after regression to Sequence 4.",
                        "NPCs must be alive, loaded, and retain the native controller link. Unloaded NPCs do not count.",
                        "Supported Sequences: 1 to 9, plus non-Beyonders. Sequence 0 is not counted by this source.")
                .push("subordinates");

        SUBORDINATE_SEQ1 = b.comment(
                        "Unique Anchors generated by one Sequence 1 subordinate."
                )
                .defineInRange("sequence_1", 10_000, 0, Integer.MAX_VALUE);

        SUBORDINATE_SEQ2 = b.comment(
                        "Unique Anchors generated by one Sequence 2 subordinate."
                )
                .defineInRange("sequence_2", 2_500, 0, Integer.MAX_VALUE);

        SUBORDINATE_SEQ3 = b.comment(
                        "Unique Anchors generated by one Sequence 3 subordinate."
                )
                .defineInRange("sequence_3", 500, 0, Integer.MAX_VALUE);

        SUBORDINATE_SEQ4 = b.comment(
                        "Unique Anchors generated by one Sequence 4 subordinate."
                )
                .defineInRange("sequence_4", 100, 0, Integer.MAX_VALUE);

        SUBORDINATE_SEQ5 = b.comment(
                        "Unique Anchors generated by one Sequence 5 subordinate."
                )
                .defineInRange("sequence_5", 50, 0, Integer.MAX_VALUE);

        SUBORDINATE_SEQ6 = b.comment(
                        "Unique Anchors generated by one Sequence 6 subordinate."
                )
                .defineInRange("sequence_6", 25, 0, Integer.MAX_VALUE);

        SUBORDINATE_SEQ7 = b.comment(
                        "Unique Anchors generated by one Sequence 7 subordinate."
                )
                .defineInRange("sequence_7", 15, 0, Integer.MAX_VALUE);

        SUBORDINATE_SEQ8 = b.comment(
                        "Unique Anchors generated by one Sequence 8 subordinate."
                )
                .defineInRange("sequence_8", 10, 0, Integer.MAX_VALUE);

        SUBORDINATE_SEQ9 = b.comment(
                        "Unique Anchors generated by one Sequence 9 subordinate."
                )
                .defineInRange("sequence_9", 10, 0, Integer.MAX_VALUE);

        SUBORDINATE_NON_BEYONDER = b.comment(
                        "Unique Anchors generated by one Non-Beyonder subordinate."
                )
                .defineInRange("non_beyonder", 10, 0, Integer.MAX_VALUE);

        b.pop();

        b.pop(); // unique_anchors

        // Quest Rewards
        b.comment("============================================================",
                        "QUESTS | NORMAL ANCHOR REWARDS",
                        "============================================================",
                        "Base points granted on first completion; these enter the persistent normal-anchor balance.",
                        "Repeated completions use anchors.quest_repeat_multiplier, except Defend Village.",
                        "Set a reward to 0 to disable it. These are quest IDs from LOTMCraft.")
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
                        "============================================================",
                        "OFFLINE DECAY | OPTIONAL LOSS OF NORMAL ANCHORS",
                        "============================================================",
                        "Disabled by default. Applies at login after the grace period.",
                        "Does not remove consenting followers, NPC links, or other unique sources.",
                        "This is separate from the fixed 20% normal-anchor death penalty."
                )
                .push("offline_decay");

        OFFLINE_DECAY = b.comment("Enable loss of normal anchors during extended offline periods.")
                .define("enabled", false);

        OFFLINE_GRACE_HOURS = b.comment("Real-world hours offline before decay starts. 72 = three days.").defineInRange(
                "grace_hours",
                72,
                0,
                Integer.MAX_VALUE
        );

        OFFLINE_DECAY_PERCENT_PER_DAY = b.comment("Percentage of remaining normal anchors lost per 24 hours of eligible absence.",
                "2.0 means 2%, not a fraction. Loss is compounded and scaled to interval_hours.").defineInRange(
                "percent_per_day",
                2.0D,
                0.0D,
                100.0D
        );

        OFFLINE_DECAY_INTERVAL_HOURS = b.comment("Apply decay only for complete intervals after the grace period, in real-world hours.",
                "24 = daily; 12 = twice daily at the equivalent daily percentage.").defineInRange(
                "interval_hours",
                24,
                1,
                Integer.MAX_VALUE
        );

        b.pop();

        b.comment("============================================================",
                        "SANITY | OPTIONAL INTEGRATION WITH LOTMCRAFT",
                        "============================================================",
                        "Disabled by default. Applies only to living survival/adventure players at Sequences 3 to 0.",
                        "Uses the requirement for the CURRENT Sequence, not the next advancement.",
                        "Deficit = 1 - total anchors / current requirement, limited to 0..1.",
                        "Sanity ceiling = native effective maximum * (1 - max_penalty * deficit).",
                        "Sanity above this ceiling falls gradually. Sanity below it is never increased by the addon.",
                        "Sufficient anchors release the ceiling; other native sanity losses and recovery still apply.",
                        "This uses native setSanityAndSync, not an attack: Persona blocking and native damage multipliers are not applied again.",
                        "NPC unloading can reduce anchors; choose a grace period and source balance appropriate for your server.")
                .push("anchor_sanity");
        ANCHOR_SANITY_ENABLED = b.comment("Enable anchor-based sanity pressure. Turn on after reviewing the values below.")
                .define("enabled", false);
        ANCHOR_SANITY_INTERVAL = b.comment("Server simulation seconds between checks. No pressure is applied while offline.")
                .defineInRange("check_interval_seconds", 5, 1, 60);
        ANCHOR_SANITY_GRACE = b.comment("Server simulation seconds of grace after login, respawn, or a change of Sequence.")
                .defineInRange("grace_seconds", 120, 0, 3600);
        ANCHOR_SANITY_DEFICIT_GRACE = b.comment("Additional simulation seconds of continuous anchor deficit before pressure starts.",
                        "Reset when anchors become sufficient. Helps tolerate short NPC/chunk-loading interruptions.")
                .defineInRange("deficit_grace_seconds", 30, 0, 3600);
        ANCHOR_SANITY_DROP = b.comment("Maximum reduction per simulation second, as a fraction of the native effective sanity maximum.",
                        "0.005 = 0.5 percentage points per second. Applied at each check; this is not the target ceiling.")
                .defineInRange("max_drop_per_second", 0.005D, 0.0D, 1.0D);
        ANCHOR_SANITY_WARNINGS = b.comment("Notify once when anchor pressure starts; notify again after recovery or a Sequence change.")
                .define("announce_pressure", true);
        b.comment("Maximum reduction of the native sanity maximum when the player has zero anchors.",
                        "Fractions, not percentages. 0.65 means a 65% reduction, leaving a 35% ceiling.",
                        "Actual pressure scales with the deficit. 0 disables pressure at that Sequence.")
                .push("max_penalty");
        double[] penalties = {0.65, 0.50, 0.30, 0.15};
        for (int sequence = 0; sequence <= 3; sequence++) {
            ANCHOR_SANITY_PENALTIES[sequence] = b.defineInRange("sequence_" + sequence, penalties[sequence], 0.0D, 1.0D);
        }
        b.pop(); b.pop();
        return b.build();
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
    public static boolean anchorSanityEnabled() { return ANCHOR_SANITY_ENABLED.get(); }
    public static boolean anchorSanityWarnings() { return ANCHOR_SANITY_WARNINGS.get(); }
    public static int anchorSanityIntervalSeconds() { return ANCHOR_SANITY_INTERVAL.get(); }
    public static int anchorSanityGraceSeconds() { return ANCHOR_SANITY_GRACE.get(); }
    public static int anchorSanityDeficitGraceSeconds() { return ANCHOR_SANITY_DEFICIT_GRACE.get(); }
    public static double anchorSanityDropPerSecond() { return ANCHOR_SANITY_DROP.get(); }
    public static double anchorSanityPenalty(int sequence) {
        return sequence >= 0 && sequence <= 3 ? ANCHOR_SANITY_PENALTIES[sequence].get() : 0;
    }

    public static long followerAnchors(int sequence) {
        return sequence < 0 || sequence > 10 ? 0 : FOLLOWER_SEQUENCE_ANCHORS[sequence].get();
    }
    public static int followerInviteSeconds() { return FOLLOWER_INVITE_SECONDS.get(); }
    public static int followerOfferCooldownSeconds() { return FOLLOWER_OFFER_COOLDOWN_SECONDS.get(); }

    public static double questRepeatMultiplier() {
        return QUEST_REPEAT_MULTIPLIER.get();
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

    // Subordinate values

    public static long subordinateSequence1Anchors() { return SUBORDINATE_SEQ1.get(); }

    public static long subordinateSequence2Anchors() { return SUBORDINATE_SEQ2.get(); }

    public static long subordinateSequence3Anchors() { return SUBORDINATE_SEQ3.get(); }

    public static long subordinateSequence4Anchors() { return SUBORDINATE_SEQ4.get(); }

    public static long subordinateSequence5Anchors() { return SUBORDINATE_SEQ5.get(); }

    public static long subordinateSequence6Anchors() { return SUBORDINATE_SEQ6.get(); }

    public static long subordinateSequence7Anchors() { return SUBORDINATE_SEQ7.get(); }

    public static long subordinateSequence8Anchors() { return SUBORDINATE_SEQ8.get(); }

    public static long subordinateSequence9Anchors() { return SUBORDINATE_SEQ9.get(); }

    public static long subordinateNonBeyonderAnchors() { return SUBORDINATE_NON_BEYONDER.get(); }

    public static long subordinateAnchors(int sequence) {
        return switch (sequence) {
            case 1 -> subordinateSequence1Anchors();
            case 2 -> subordinateSequence2Anchors();
            case 3 -> subordinateSequence3Anchors();
            case 4 -> subordinateSequence4Anchors();
            case 5 -> subordinateSequence5Anchors();
            case 6 -> subordinateSequence6Anchors();
            case 7 -> subordinateSequence7Anchors();
            case 8 -> subordinateSequence8Anchors();
            case 9 -> subordinateSequence9Anchors();
            case 10 -> subordinateNonBeyonderAnchors();
            default -> 0;
        };
    }

    public static long uniqueSourceAnchors(int category, int sequence) {
        if (category == 1 || category == 2) return subordinateAnchors(sequence);
        if (category == 3) return switch (sequence) {
            case 1 -> errorAvatarSequence1Anchors(); case 2 -> errorAvatarSequence2Anchors();
            case 3 -> errorAvatarSequence3Anchors(); case 4 -> errorAvatarSequence4Anchors();
            case 5 -> errorAvatarSequence5Anchors(); case 6 -> errorAvatarSequence6Anchors(); default -> 0;
        };
        if (category == 4) return switch (sequence) {
            case 1 -> marionetteSequence1Anchors(); case 2 -> marionetteSequence2Anchors();
            case 3 -> marionetteSequence3Anchors(); case 4 -> marionetteSequence4Anchors();
            case 5 -> marionetteSequence5Anchors(); case 6 -> marionetteSequence6Anchors();
            case 7 -> marionetteSequence7Anchors(); case 8 -> marionetteSequence8Anchors();
            case 9 -> marionetteSequence9Anchors(); case 10 -> marionetteNonBeyonderAnchors(); default -> 0;
        };
        if (category == 5) return switch (sequence) {
            case 1 -> personaSequence1Anchors(); case 2 -> personaSequence2Anchors();
            case 3 -> personaSequence3Anchors(); case 4 -> personaSequence4Anchors();
            case 5 -> personaSequence5Anchors(); default -> 0;
        };
        return 0;
    }

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

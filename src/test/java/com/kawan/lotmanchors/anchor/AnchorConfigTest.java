package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.config.AnchorConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/** Exercises the actual NeoForge spec and NightConfig writer without starting Minecraft. */
public final class AnchorConfigTest {
    private static int checks;
    public static void main(String[] args) throws Exception {
        var spec = AnchorConfig.createSpec();
        CommentedConfig fresh = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
        spec.correct(fresh);
        check(spec.isCorrect(fresh), "generated defaults satisfy spec");
        check(fresh.getInt("anchors.required_sequence_0") == 30_000, "global requirement unchanged");
        check(fresh.getInt("follower_values.sequence_0") == 30_000, "deity follower value unchanged");
        check(fresh.getInt("unique_anchors.error_avatars.sequence_1") == 10_000, "Error table remains separate");
        check(fresh.getInt("unique_anchors.marionettes.non_beyonder") == 10, "Fool table remains separate");
        check(fresh.getInt("unique_anchors.personas.sequence_5") == 50, "Visionary table remains separate");
        check(fresh.getInt("unique_anchors.subordinates.sequence_4") == 100, "Red Priest table remains separate");
        check(!fresh.<Boolean>get("offline_decay.enabled"), "offline decay stays disabled");
        check(!fresh.<Boolean>get("anchor_sanity.enabled"), "sanity integration opt-in");
        check(fresh.<Double>get("anchor_sanity.max_penalty.sequence_0") == 0.65
                && fresh.getInt("anchor_sanity.grace_seconds") == 120
                && fresh.getInt("anchor_sanity.deficit_grace_seconds") == 30, "sanity pressure defaults");
        check(!fresh.contains("anchors.prayer_reward") && !fresh.contains("anchors.follower_anchors")
                && !fresh.contains("anchors.prayer_cooldown_minutes") && !fresh.contains("anchors.announce_prayer_reward"),
                "obsolete settings omitted");
        CommentedConfig existing = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
        spec.correct(existing);
        existing.set("anchors.required_sequence_3", 1444);
        existing.set("anchors.follower_invite_seconds", 180);
        existing.set("anchors.quest_repeat_multiplier", 0.42);
        existing.set("follower_values.sequence_2", 2345);
        existing.set("unique_anchors.error_avatars.sequence_1", 12345);
        existing.set("unique_anchors.marionettes.non_beyonder", 99);
        existing.set("unique_anchors.personas.sequence_5", 321);
        existing.set("unique_anchors.subordinates.sequence_4", 345);
        existing.set("quests.help_beyonder", 543);
        existing.set("offline_decay.percent_per_day", 7.5);
        existing.set("anchors.prayer_reward", 999);
        String[] editedPaths = {"anchors.required_sequence_3", "anchors.follower_invite_seconds",
                "anchors.quest_repeat_multiplier", "follower_values.sequence_2", "unique_anchors.error_avatars.sequence_1",
                "unique_anchors.marionettes.non_beyonder", "unique_anchors.personas.sequence_5",
                "unique_anchors.subordinates.sequence_4", "quests.help_beyonder", "offline_decay.percent_per_day"};
        java.util.Map<String, Object> before = new java.util.HashMap<>();
        for (String key : editedPaths) before.put(key, existing.get(key));
        spec.correct(existing);
        for (String key : editedPaths) {
            check(java.util.Objects.equals(before.get(key), existing.get(key)), "preserves existing edit: " + key);
        }
        check(!existing.contains("anchors.prayer_reward"), "obsolete existing key cleaned without changing live values");
        check(spec.isCorrect(existing), "corrected old configuration valid");
        var writer = new TomlWriter();
        StringWriter output = new StringWriter();
        writer.write(fresh, output);
        String toml = output.toString();
        CommentedConfig parsed = new TomlParser().parse(new StringReader(toml));
        check(spec.isCorrect(parsed), "generated TOML parses and validates");
        check(parsed.getInt("anchors.required_sequence_3") == 1000
                && parsed.getInt("anchors.follower_invite_seconds") == 120
                && parsed.getInt("follower_values.sequence_2") == 2500, "example contains defaults, not customized test values");
        check(toml.contains("GLOBAL SERVER RULES") && toml.contains("FAITH | POINTS")
                && toml.contains("RED PRIEST | TEAM") && toml.contains("OFFLINE DECAY"), "readable English section headings");
        check(toml.indexOf("required_sequence_0") < toml.indexOf("required_sequence_3"), "strongest-first requirements");
        long day = 24 * 3_600_000L;
        check(OfflineAnchorDecay.remaining(1000, 3 * day, 72, 2, 24) == 1000, "grace period");
        check(OfflineAnchorDecay.remaining(1000, 4 * day - 1, 72, 2, 24) == 1000, "partial interval");
        check(OfflineAnchorDecay.remaining(1000, 4 * day, 72, 2, 24) == 980, "configured 2 percent, not fixed 20 percent");
        check(OfflineAnchorDecay.remaining(1000, 5 * day, 72, 2, 24) == 960, "compounded daily loss");
        check(OfflineAnchorDecay.remaining(1000, 4 * day, 72, 2, 12) == 980, "daily percentage independent of interval");
        check(OfflineAnchorDecay.remaining(1000, 4 * day, 72, 0, 24) == 1000, "zero disables loss");
        check(OfflineAnchorDecay.remaining(1000, 4 * day, 72, 100, 24) == 0, "full loss");
        check(OfflineAnchorDecay.remaining(0, 4 * day, 72, 2, 24) == 0, "empty balance");
        if (args.length > 0) {
            Path target = Path.of(args[0]);
            Files.createDirectories(target.toAbsolutePath().getParent());
            Files.writeString(target, toml);
            System.out.println("Generated example: " + target.toAbsolutePath());
        }
        System.out.println("AnchorConfig: " + checks + " checks passed");
    }
    private static void check(boolean condition, String scenario) {
        if (!condition) throw new AssertionError(scenario);
        checks++;
    }
}

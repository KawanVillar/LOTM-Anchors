package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.network.AnchorSourcesNetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;

/** Packet and preview checks without a Minecraft client, server, or real players. */
public final class AnchorSourcesPanelTest {
    private static int checks;
    public static void main(String[] args) {
        UUID owner = new UUID(1, 1), other = new UUID(2, 2);
        check(AnchorSourcesNetwork.canView(owner, owner, false), "owner can read");
        check(!AnchorSourcesNetwork.canView(other, owner, false), "stranger cannot read");
        check(AnchorSourcesNetwork.canView(other, owner, true), "operator can inspect");
        check(AnchorSourcesNetwork.canRemove(owner, owner), "owner can request removal");
        check(!AnchorSourcesNetwork.canRemove(other, owner), "inspection grants no removal rights");
        FollowerBook book = new FollowerBook();
        book.restore(other, owner);
        check(!book.remove(new UUID(3, 3), other), "foreign deity cannot remove target follower");
        var snapshot = AnchorPanelFixtures.create(owner);
        String[] pathways = {"red_priest", "error", "fool", "visionary"};
        String[] sources = {"subordinate", "error_avatar", "marionette", "persona"};
        for (int i = 0; i < pathways.length; i++) {
            for (int j = 0; j < sources.length; j++) {
                check(PathwayAnchorRules.allows(pathways[i], sources[j]) == (i == j),
                        "exclusive source mapping: " + pathways[i] + "/" + sources[j]);
            }
        }
        check(!PathwayAnchorRules.allows(null, "subordinate") && !PathwayAnchorRules.allows("none", "subordinate"),
                "missing pathway cannot expose Red Priest sources");
        check(PathwayAnchorRules.allows("ERROR", "error_avatar"), "pathway matching ignores case");
        check(PathwayAnchorRules.panelCategories("red_priest").equals(java.util.List.of(0, 1, 2)), "Red Priest tabs");
        check(PathwayAnchorRules.panelCategories("error").equals(java.util.List.of(0, 3)), "Error avatar tab");
        check(PathwayAnchorRules.panelCategories("fool").equals(java.util.List.of(0, 4)), "Fool marionette tab");
        check(PathwayAnchorRules.panelCategories("visionary").equals(java.util.List.of(0, 5)), "Visionary persona tab");
        for (String pathway : java.util.List.of("error", "fool", "visionary", "sun", "none")) {
            check(!PathwayAnchorRules.panelCategories(pathway).contains(1)
                    && !PathwayAnchorRules.panelCategories(pathway).contains(2), "no Red Priest tabs for " + pathway);
            var preview = AnchorPanelFixtures.create(owner, pathway);
            check(preview.rows().stream().allMatch(row -> PathwayAnchorRules.panelCategories(pathway).contains(row.category()))
                    && preview.teamCount() == 0 && preview.npcCount() == 0 && preview.pathway().equals(pathway),
                    "preview respects inspected pathway " + pathway);
            check(preview.stability() == preview.normal() + preview.panelTotal(), "preview totals include all visible unique sources");
            FriendlyByteBuf roundTrip = new FriendlyByteBuf(Unpooled.buffer());
            try {
                AnchorSourcesNetwork.Snapshot.CODEC.encode(roundTrip, preview);
                check(preview.equals(AnchorSourcesNetwork.Snapshot.CODEC.decode(roundTrip)), "all pathway categories survive packet round trip");
            } finally { roundTrip.release(); }
        }
        var fool = AnchorPanelFixtures.create(owner, "fool");
        check(fool.count(4) == 3 && fool.total(4) == 560, "marionette details include non-Beyonders");
        var oneMarionette = AnchorSourcesNetwork.Snapshot.fromRows(owner, "Test", "fool", 0, java.util.List.of(
                new AnchorSourceRow(4, new UUID(0, 100), "Rogue Beyonder", 3, 500, true, "Loaded", "Native link confirmed.")));
        check(oneMarionette.panelTotal() == 500 && oneMarionette.stability() == 500 && oneMarionette.count(4) == 1,
                "single Sequence 3 marionette appears in panel and contributes the same 500 points");
        var many = AnchorSourcesNetwork.Snapshot.fromRows(owner, "Test", "fool", 0,
                java.util.Collections.nCopies(257, oneMarionette.rows().getFirst()));
        check(many.truncated() && many.rows().size() == 256 && many.count(4) == 257 && many.total(4) == 128_500,
                "truncated rows retain full unique source totals");
        var error = AnchorPanelFixtures.create(owner, "error");
        check(error.count(3) == 2 && error.total(3) == 2600, "avatar details and totals");
        var visionary = AnchorPanelFixtures.create(owner, "visionary");
        check(visionary.count(5) == 3 && visionary.total(5) == 600, "unloaded persona avatar is listed without granting anchors");
        check(visionary.rows().stream().anyMatch(row -> row.category() == 5 && !row.active() && row.sequence() == -1 && row.value() == 0),
                "unloaded persona has explicit unavailable state");
        var stale = AnchorSourcesNetwork.Snapshot.fromRows(owner, "Test", "fool", 0, snapshot.rows());
        check(stale.team() == 0 && stale.npcs() == 0, "snapshot builder drops foreign pathway sources");
        var mixed = java.util.List.of(
                new AnchorSourceRow(2, new UUID(0, 1), "AAA", 4, 100, true, "Loaded", ""),
                new AnchorSourceRow(2, new UUID(0, 2), "ZZZ", 1, 10_000, true, "Loaded", ""),
                new AnchorSourceRow(2, new UUID(0, 3), "BBB", 10, 10, true, "Loaded", ""),
                new AnchorSourceRow(2, new UUID(0, 4), "CCC", -1, 0, false, "Offline", ""),
                new AnchorSourceRow(2, new UUID(0, 5), "DDD", 0, 0, false, "Loaded", ""));
        var ordered = mixed.stream().sorted(AnchorSourceRow.DISPLAY_ORDER).toList();
        check(ordered.stream().map(AnchorSourceRow::sequence).toList().equals(java.util.List.of(0, 1, 4, 10, -1)),
                "strongest Sequence first regardless of name or contribution");
        check(snapshot.owner().equals(owner), "preview ownership");
        check(snapshot.rows().stream().filter(row -> row.category() == 0).count() == snapshot.followerCount(), "follower count");
        check(snapshot.rows().stream().filter(row -> row.category() == 1).count() == snapshot.teamCount(), "team count");
        check(snapshot.rows().stream().filter(row -> row.category() == 2).count() == snapshot.npcCount(), "NPC count");
        check(snapshot.rows().stream().filter(row -> row.category() == 0).mapToLong(AnchorSourceRow::value).sum() == snapshot.followers(), "follower total");
        check(snapshot.rows().stream().filter(row -> row.category() == 1).mapToLong(AnchorSourceRow::value).sum() == snapshot.team(), "team total");
        check(snapshot.rows().stream().filter(row -> row.category() == 2).mapToLong(AnchorSourceRow::value).sum() == snapshot.npcs(), "NPC total");
        check(snapshot.rows().stream().filter(row -> row.category() == 0 && row.state().equals("Offline"))
                .allMatch(row -> row.active() && row.value() == 100), "offline faithful keeps cached contribution");
        check(snapshot.rows().stream().filter(row -> row.category() == 1 && row.state().equals("Offline"))
                .allMatch(row -> !row.active() && row.value() == 0), "offline Team is distinct from faith");
        check(snapshot.stability() == snapshot.normal() + snapshot.followers() + snapshot.team() + snapshot.npcs(), "demo totals agree");
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            AnchorSourcesNetwork.Snapshot.CODEC.encode(buf, snapshot);
            check(snapshot.equals(AnchorSourcesNetwork.Snapshot.CODEC.decode(buf)), "snapshot packet round trip");
            check(buf.readableBytes() == 0, "all snapshot bytes consumed");
        } finally { buf.release(); }
        buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var request = new AnchorSourcesNetwork.Request(owner, other);
            AnchorSourcesNetwork.Request.CODEC.encode(buf, request);
            check(request.equals(AnchorSourcesNetwork.Request.CODEC.decode(buf)), "request packet round trip");
        } finally { buf.release(); }
        buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            AnchorSourcesNetwork.Snapshot.CODEC.encode(buf, snapshot);
            var invalid = new AnchorSourcesNetwork.Snapshot(owner, "X", "red_priest", 0, 0, snapshot.categories(), true,
                    java.util.Collections.nCopies(257, snapshot.rows().getFirst()));
            buf.clear();
            AnchorSourcesNetwork.Snapshot.CODEC.encode(buf, invalid);
            boolean rejected = false;
            try { AnchorSourcesNetwork.Snapshot.CODEC.decode(buf); } catch (IllegalArgumentException expected) { rejected = true; }
            check(rejected, "oversized packets rejected");
        } finally { buf.release(); }
        System.out.println("AnchorSourcesPanel: " + checks + " checks passed");
    }
    private static void check(boolean condition, String scenario) {
        if (!condition) throw new AssertionError(scenario);
        checks++;
    }
}

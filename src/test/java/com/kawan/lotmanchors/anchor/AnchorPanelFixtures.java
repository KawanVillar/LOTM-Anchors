package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.anchor.AnchorSourceRow;
import com.kawan.lotmanchors.anchor.PathwayAnchorRules;
import com.kawan.lotmanchors.network.AnchorSourcesNetwork;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Synthetic fixtures for offline packet and category regression tests only. */
public final class AnchorPanelFixtures {
    private AnchorPanelFixtures() {}
    public static AnchorSourcesNetwork.Snapshot create(UUID owner) { return create(owner, "red_priest"); }
    public static AnchorSourcesNetwork.Snapshot create(UUID owner, String pathway) {
        List<AnchorSourceRow> rows = new ArrayList<>(List.of(
                row(0, 1, "Audrey", 3, 500, true, "Online", "Bond accepted. Current Sequence."),
                row(0, 2, "Alger", 4, 100, true, "Offline", "Bond retained. Last known Sequence."),
                row(0, 3, "Leonard", 5, 50, true, "Online", "Bond accepted. Current Sequence.")));
        if (PathwayAnchorRules.allows(pathway, "subordinate")) rows.addAll(List.of(
                row(1, 4, "Soldier A", 5, 50, true, "Online", "Valid Team member."),
                row(1, 5, "Soldier B", -1, 0, false, "Offline", "Offline Team members do not contribute."),
                row(2, 6, "Captain", 1, 10_000, true, "Loaded", "Valid subordinate. Dimension: minecraft:overworld"),
                row(2, 7, "Veteran", 1, 10_000, true, "Loaded", "Valid subordinate. Dimension: minecraft:overworld"),
                row(2, 8, "Scout", 4, 100, true, "Loaded", "Valid subordinate. Dimension: minecraft:overworld")));
        if (PathwayAnchorRules.allows(pathway, "marionette")) rows.addAll(List.of(
                row(4, 9, "Rogue Beyonder", 3, 500, true, "Loaded", "Native marionette/controller link confirmed. Dimension: minecraft:overworld"),
                row(4, 10, "Hunter", 5, 50, true, "Loaded", "Native marionette/controller link confirmed. Dimension: minecraft:the_nether"),
                row(4, 11, "Villager", 10, 10, true, "Loaded", "Native marionette/controller link confirmed. Dimension: minecraft:overworld")));
        if (PathwayAnchorRules.allows(pathway, "error_avatar")) rows.addAll(List.of(
                row(3, 12, "Avatar A", 2, 2500, true, "Loaded", "Original owner confirmed. Pathway: error. Dimension: minecraft:overworld"),
                row(3, 13, "Avatar B", 4, 100, true, "Loaded", "Original owner confirmed. Pathway: error. Dimension: minecraft:overworld")));
        if (PathwayAnchorRules.allows(pathway, "persona")) rows.addAll(List.of(
                row(5, 14, "Player A / Persona 1", 3, 500, true, "Mind link", "Native Persona link. UUID identifies the online host."),
                row(5, 15, "Persona avatar", 4, 100, true, "Loaded avatar", "Registered Persona avatar; original owner confirmed."),
                row(5, 16, "Registered Persona avatar", -1, 0, false, "Not loaded", "Ownership and Sequence cannot be verified. No contribution.")));
        return AnchorSourcesNetwork.Snapshot.fromRows(owner, "Example", pathway, 500, rows);
    }
    private static AnchorSourceRow row(int category, int id, String name, int sequence, long value,
                                       boolean active, String state, String reason) {
        return new AnchorSourceRow(category, new UUID(0, id), name, sequence, value, active, state, reason);
    }
}
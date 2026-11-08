package com.kawan.lotmanchors.anchor;

import java.util.UUID;

/** Read-only server snapshot. Identity is never supplied by a display name. */
public record AnchorSourceRow(int category, UUID id, String name, int sequence,
                             long value, boolean active, String state, String reason) {
    public static final int FOLLOWER = 0, TEAM = 1, NPC = 2, AVATAR = 3, MARIONETTE = 4, PERSONA = 5;
    public static final int CATEGORY_COUNT = 6;
    public static String label(int category) {
        return switch (category) {
            case FOLLOWER -> "Followers";
            case TEAM -> "Team";
            case NPC -> "NPCs";
            case AVATAR -> "Error Avatars";
            case MARIONETTE -> "Marionettes";
            case PERSONA -> "Virtual Personas";
            default -> "Unknown";
        };
    }
    public static String description(int category) {
        return switch (category) {
            case FOLLOWER -> "Players who accepted a faith bond. Offline followers still count.";
            case TEAM -> "Players in the native Red Priest team. Only valid online members in your dimension contribute.";
            case NPC -> "NPC subordinates linked to you. Only loaded, living NPCs in your dimension contribute.";
            case AVATAR -> "Loaded Error avatars in your dimension linked to their original owner. Supported Sequences: 1 to 6.";
            case MARIONETTE -> "Loaded entities in your dimension controlled through native spirit body threads. Sequences 1 to 9 and non-Beyonders.";
            case PERSONA -> "Native Persona links and registered avatars available in your current dimension. Supported Sequences: 1 to 5.";
            default -> "";
        };
    }
    /** Strongest Sequence first; non-Beyonders and unknown Sequences come last. */
    public static int sequenceRank(int sequence) { return sequence >= 0 && sequence <= 10 ? sequence : 11; }
    public static final java.util.Comparator<AnchorSourceRow> DISPLAY_ORDER =
            java.util.Comparator.comparingInt(AnchorSourceRow::category)
                    .thenComparingInt(row -> sequenceRank(row.sequence()))
                    .thenComparing(AnchorSourceRow::name, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(row -> row.id().toString());
}

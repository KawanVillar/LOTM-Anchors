package com.kawan.lotmanchors.anchor;

import java.util.List;

/** Visibility follows the inspected player's pathway, not the viewer's pathway. */
public final class PathwayAnchorRules {
    private PathwayAnchorRules() {}
    public static boolean allows(String pathway, String source) {
        if (pathway == null || source == null) return false;
        String required = switch (source) {
            case "subordinate" -> "red_priest";
            case "error_avatar" -> "error";
            case "marionette" -> "fool";
            case "persona" -> "visionary";
            default -> "";
        };
        return !required.isEmpty() && required.equalsIgnoreCase(pathway);
    }
    public static List<Integer> panelCategories(String pathway) {
        if (allows(pathway, "subordinate")) return List.of(0, 1, 2);
        if (allows(pathway, "error_avatar")) return List.of(0, 3);
        if (allows(pathway, "marionette")) return List.of(0, 4);
        if (allows(pathway, "persona")) return List.of(0, 5);
        return List.of(0);
    }
}

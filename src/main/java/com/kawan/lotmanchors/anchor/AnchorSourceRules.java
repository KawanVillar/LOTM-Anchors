package com.kawan.lotmanchors.anchor;

import java.util.*;

/** One entity body contributes once; independent faith and mental links are separate bonds. */
public final class AnchorSourceRules {
    private AnchorSourceRules() {}
    private static boolean body(AnchorSourceRow row) {
        return row.category() >= 2 && !(row.category() == 5 && row.state().equals("Mind link"));
    }
    private static int priority(AnchorSourceRow row) {
        return switch (row.category()) { case 4 -> 4; case 5 -> 3; case 3 -> 2; default -> 1; };
    }
    public static List<AnchorSourceRow> normalize(List<AnchorSourceRow> rows) {
        Map<UUID, AnchorSourceRow> bodies = new LinkedHashMap<>();
        List<AnchorSourceRow> result = new ArrayList<>();
        for (var row : rows) {
            if (!body(row)) { result.add(row); continue; }
            bodies.merge(row.id(), row, (a, b) -> priority(b) > priority(a) ? b : a);
        }
        result.addAll(bodies.values());
        result.sort(AnchorSourceRow.DISPLAY_ORDER);
        return List.copyOf(result);
    }
}

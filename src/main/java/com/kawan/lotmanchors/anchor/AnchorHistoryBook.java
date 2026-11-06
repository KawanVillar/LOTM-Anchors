package com.kawan.lotmanchors.anchor;
import java.util.*;
/** Bounded per-owner history. Source changes must persist before being recorded. */
public final class AnchorHistoryBook {
    public static final int LIMIT = 32;
    public record Entry(long time, String message) {}
    private final Map<UUID, List<Entry>> entries = new HashMap<>();
    private final Map<UUID, Map<Integer, Long>> observed = new HashMap<>();
    private final Map<UUID, Map<Integer, Long>> pending = new HashMap<>();
    private final Map<UUID, Long> since = new HashMap<>();
    public List<Entry> entries(UUID owner) { return List.copyOf(entries.getOrDefault(owner, List.of())); }
    public Set<UUID> owners() { return Set.copyOf(entries.keySet()); }
    public void add(UUID owner, long time, String message) {
        var list = entries.computeIfAbsent(owner, id -> new ArrayList<>());
        list.addFirst(new Entry(time, message.substring(0, Math.min(240, message.length()))));
        if (list.size() > LIMIT) list.removeLast();
    }
    public List<String> observe(UUID owner, Map<Integer, Long> totals, long tick) {
        totals = Map.copyOf(totals);
        var previous = observed.putIfAbsent(owner, totals);
        if (previous == null) return List.of();
        if (previous.equals(totals)) { pending.remove(owner); since.remove(owner); return List.of(); }
        if (!totals.equals(pending.get(owner)) || tick < since.getOrDefault(owner, tick)) {
            pending.put(owner, totals); since.put(owner, tick); return List.of();
        }
        if (tick - since.get(owner) < 600) return List.of();
        List<String> changes = new ArrayList<>();
        for (int category = 1; category < AnchorSourceRow.CATEGORY_COUNT; category++) {
            long before = previous.getOrDefault(category, 0L), after = totals.getOrDefault(category, 0L);
            if (before != after) changes.add(AnchorSourceRow.label(category) + ": " + before + " -> " + after + " anchors (observed)");
        }
        observed.put(owner, totals); pending.remove(owner); since.remove(owner);
        return changes;
    }
    public void forget(UUID owner) { observed.remove(owner); pending.remove(owner); since.remove(owner); }
}

package com.kawan.lotmanchors.anchor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** One replaceable value per scope, valid only for the exact server tick. Server-thread only. */
public final class ServerTickCache<K, V> {
    private record Entry<V>(long tick, V value) {}
    private final Map<K, Entry<V>> entries = new HashMap<>();

    public V get(K scope, long tick, Supplier<V> build) {
        Entry<V> entry = entries.get(scope);
        if (entry != null && entry.tick() == tick) return entry.value();
        V value = Objects.requireNonNull(build.get());
        entries.put(scope, new Entry<>(tick, value));
        return value;
    }

    public void invalidate(K scope) { entries.remove(scope); }
    public void clear() { entries.clear(); }
}

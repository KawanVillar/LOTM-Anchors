package com.kawan.lotmanchors.anchor;

import java.util.*;
import java.util.function.Function;

/** Candidate lookup only. A caller must revalidate native ownership before awarding points. */
public final class OwnedSourceIndex<T> {
    private final Map<UUID, List<T>> byOwner;

    private OwnedSourceIndex(Map<UUID, List<T>> byOwner) { this.byOwner = byOwner; }

    public static <T> OwnedSourceIndex<T> build(Iterable<T> sources, Function<T, UUID> identity,
                                               Function<T, Collection<UUID>> owners) {
        Map<UUID, List<T>> index = new HashMap<>();
        Set<UUID> seen = new HashSet<>();
        for (T source : sources) {
            if (!seen.add(identity.apply(source))) continue;
            for (UUID owner : new HashSet<>(owners.apply(source))) {
                if (owner != null) index.computeIfAbsent(owner, key -> new ArrayList<>()).add(source);
            }
        }
        index.replaceAll((owner, list) -> List.copyOf(list));
        return new OwnedSourceIndex<>(Map.copyOf(index));
    }

    public List<T> candidates(UUID owner) { return byOwner.getOrDefault(owner, List.of()); }
}

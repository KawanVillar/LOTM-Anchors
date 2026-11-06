package com.kawan.lotmanchors.anchor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Pure validation of native Team data; never changes the main mod's roster. */
public final class TeamAnchorRules {
    private TeamAnchorRules() {}

    public static Set<UUID> validRoster(UUID leader, String recordedLeader,
                                        List<String> members, int maxMembers) {
        if (leader == null || !leader.equals(parse(recordedLeader))
                || members == null || maxMembers <= 0 || members.size() > maxMembers) {
            return Set.of();
        }
        Set<UUID> result = new HashSet<>();
        for (String value : members) {
            UUID member = parse(value);
            // Corrupted records contribute nothing; do not repair native data here.
            if (member == null || leader.equals(member) || !result.add(member)) {
                return Set.of();
            }
        }
        return Set.copyOf(result);
    }

    public static boolean canContribute(UUID leader, Set<UUID> roster, UUID member,
                                       String recordedLeader, boolean alive, boolean spectator) {
        return leader != null && member != null && alive && !spectator
                && !leader.equals(member) && roster.contains(member)
                && leader.equals(parse(recordedLeader));
    }

    private static UUID parse(String value) {
        if (value == null) return null;
        try {
            UUID uuid = UUID.fromString(value);
            return uuid.toString().equalsIgnoreCase(value) ? uuid : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}

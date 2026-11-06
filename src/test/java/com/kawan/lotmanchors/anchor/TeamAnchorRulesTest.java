package com.kawan.lotmanchors.anchor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Runs without Minecraft, a game server or additional test libraries. */
public final class TeamAnchorRulesTest {
    private static int checks;

    public static void main(String[] args) {
        UUID leader = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID member = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID other = UUID.fromString("00000000-0000-0000-0000-000000000003");
        String owner = leader.toString();
        List<String> members = List.of(member.toString());
        Set<UUID> roster = TeamAnchorRules.validRoster(leader, owner, members, 4);
        check(roster.equals(Set.of(member)), "valid roster");
        check(TeamAnchorRules.canContribute(leader, roster, member, owner, true, false), "valid member");
        check(!TeamAnchorRules.canContribute(leader, roster, leader, owner, true, false), "self");
        check(!TeamAnchorRules.canContribute(leader, roster, other, owner, true, false), "outsider");
        check(!TeamAnchorRules.canContribute(leader, roster, member, other.toString(), true, false), "changed leader");
        check(!TeamAnchorRules.canContribute(leader, roster, member, "", true, false), "disbanded member");
        check(!TeamAnchorRules.canContribute(leader, roster, member, null, true, false), "missing leader");
        check(!TeamAnchorRules.canContribute(leader, roster, member, owner, false, false), "dead");
        check(!TeamAnchorRules.canContribute(leader, roster, member, owner, true, true), "spectator");
        check(!TeamAnchorRules.canContribute(leader, Set.of(), member, owner, true, false), "removed member");
        check(TeamAnchorRules.validRoster(leader, "", members, 4).isEmpty(), "missing owner");
        check(TeamAnchorRules.validRoster(leader, other.toString(), members, 4).isEmpty(), "foreign owner");
        check(TeamAnchorRules.validRoster(leader, owner, List.of(member.toString(), member.toString()), 4).isEmpty(), "duplicate");
        check(TeamAnchorRules.validRoster(leader, owner, List.of(owner), 4).isEmpty(), "leader in roster");
        check(TeamAnchorRules.validRoster(leader, owner, List.of("invalid"), 4).isEmpty(), "malformed UUID");
        check(TeamAnchorRules.validRoster(leader, owner, List.of("1-1-1-1-1"), 4).isEmpty(), "abbreviated UUID");
        check(TeamAnchorRules.validRoster(leader, owner, Arrays.asList((String) null), 4).isEmpty(), "null entry");
        check(TeamAnchorRules.validRoster(leader, owner, null, 4).isEmpty(), "null roster");
        check(TeamAnchorRules.validRoster(leader, owner, List.of(member.toString(), other.toString()), 1).isEmpty(), "over capacity");
        check(TeamAnchorRules.validRoster(leader, owner, members, 1).size() == 1, "at capacity");
        check(TeamAnchorRules.validRoster(leader, owner, List.of(), 4).isEmpty(), "empty roster");
        System.out.println("TeamAnchorRules: " + checks + " checks passed");
    }

    private static void check(boolean condition, String scenario) {
        if (!condition) throw new AssertionError(scenario);
        checks++;
    }
}

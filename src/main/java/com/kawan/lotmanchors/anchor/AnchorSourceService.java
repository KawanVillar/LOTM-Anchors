package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.config.AnchorConfig;
import com.kawan.lotmanchors.event.HonorificPrayerHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TeamComponent;
import de.jakob.lotm.attachments.VirtualPersonaComponent;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.TeamUtils;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;

public final class AnchorSourceService {
    private AnchorSourceService() {}

    public static List<AnchorSourceRow> allFresh(ServerPlayer owner) {
        AnchorEntityIndex.invalidate(owner.serverLevel());
        return all(owner);
    }

    public static List<AnchorSourceRow> all(ServerPlayer owner) {
        List<AnchorSourceRow> rows = new ArrayList<>(followers(owner));
        rows.addAll(subordinates(owner));
        rows.addAll(exclusive(owner));
        return AnchorSourceRules.normalize(rows);
    }

    public static List<AnchorSourceRow> exclusive(ServerPlayer owner) {
        List<AnchorSourceRow> rows = new ArrayList<>();
        boolean ownerActive = owner.isAlive() && !owner.isSpectator() && BeyonderData.isBeyonder(owner);
        Set<UUID> seen = new HashSet<>();
        for (Entity entity : AnchorEntityIndex.candidates(owner.serverLevel(), owner.getUUID())) {
            if (entity.isRemoved() || entity.level() != owner.serverLevel()) continue;
            if (!seen.add(entity.getUUID())) continue;
            if (!controlledMarionette(entity) && entity instanceof AvatarEntity avatar && owner.getUUID().equals(avatar.getOriginalOwner())
                    && "error".equalsIgnoreCase(avatar.getPathway())) {
                int seq = avatar.getSequence();
                boolean active = ownerActive && avatar.isAlive() && seq >= 1 && seq <= 6;
                rows.add(new AnchorSourceRow(3, avatar.getUUID(), avatar.getName().getString(), seq,
                        active ? AnchorConfig.uniqueSourceAnchors(3, seq) : 0, active, "Loaded",
                        (active ? "Original owner confirmed. " : !avatar.isAlive() ? "Avatar is dead. " : "Unsupported Sequence; expected 1 to 6. ")
                                + "Pathway: error. Dimension: " + entity.level().dimension().location()));
            }
            if (entity instanceof LivingEntity marionette && marionette.hasData(ModAttachments.MARIONETTE_COMPONENT)) {
                var link = marionette.getData(ModAttachments.MARIONETTE_COMPONENT);
                if (!link.isMarionette() || !owner.getStringUUID().equals(link.getControllerUUID())) continue;
                int seq = BeyonderData.getSequence(marionette);
                boolean active = ownerActive && marionette.isAlive() && seq >= 1 && seq <= 10;
                rows.add(new AnchorSourceRow(4, marionette.getUUID(), marionette.getName().getString(), seq,
                        active ? AnchorConfig.uniqueSourceAnchors(4, seq) : 0, active, "Loaded",
                        (active ? "Native marionette/controller link confirmed. " : !marionette.isAlive() ? "Marionette is dead. " : "Unsupported Sequence; expected 1 to 9 or non-Beyonder. ")
                                + "Pathway: " + BeyonderData.getPathway(marionette) + ". Dimension: " + entity.level().dimension().location()));
            }
        }
        collectPersonas(owner, rows);
        if (!ownerActive) return rows.stream().map(row -> inactive(row, "Owner must be a living Beyonder and not spectating.")).toList();
        return sorted(rows);
    }

    private static void collectPersonas(ServerPlayer owner, List<AnchorSourceRow> rows) {
        String ownerName = owner.getName().getString();
        for (ServerPlayer host : owner.server.getPlayerList().getPlayers()) {
            if (host.serverLevel() != owner.serverLevel()) continue;
            if (!host.hasData(ModAttachments.VIRTUAL_PERSONAS.get())) continue;
            VirtualPersonaComponent component = host.getData(ModAttachments.VIRTUAL_PERSONAS.get());
            int[] cumulative = new int[7];
            for (int seq = 1; seq <= 5; seq++) {
                cumulative[seq] = (int) component.getAffectedBy(seq).stream().filter(ownerName::equals).count();
            }
            int index = 0;
            // Native getAffectedBy(s) returns personas with Sequence >= s.
            for (int seq = 1; seq <= 5; seq++) {
                int count = Math.max(0, cumulative[seq] - cumulative[seq + 1]);
                for (int i = 0; i < count; i++) {
                    rows.add(new AnchorSourceRow(5, host.getUUID(), host.getName().getString() + " / Persona " + (++index), seq,
                            AnchorConfig.uniqueSourceAnchors(5, seq), true, "Mind link",
                            "Native Persona owned by " + ownerName + ". UUID identifies the online host, not a separate entity."
                                    + " Host Sequence: " + BeyonderData.getSequence(host) + ". Dimension: " + host.serverLevel().dimension().location()));
                }
            }
        }
        if (!owner.hasData(ModAttachments.VIRTUAL_PERSONAS.get())) return;
        VirtualPersonaComponent own = owner.getData(ModAttachments.VIRTUAL_PERSONAS.get());
        for (UUID id : new LinkedHashSet<>(own.getAvatars())) {
            Entity found = owner.serverLevel().getEntity(id);
            if (found instanceof AvatarEntity avatar) {
                int seq = avatar.getSequence();
                boolean owned = owner.getUUID().equals(avatar.getOriginalOwner());
                boolean active = owned && avatar.isAlive() && !controlledMarionette(avatar) && seq >= 1 && seq <= 5;
                String reason = !owned ? "Registered avatar no longer confirms this original owner."
                        : controlledMarionette(avatar) ? "Controlled as a marionette; this body contributes only through its current controller."
                        : !avatar.isAlive() ? "Avatar is dead." : !active ? "Unsupported Sequence; expected 1 to 5." : "Registered Persona avatar; original owner confirmed.";
                rows.add(new AnchorSourceRow(5, id, avatar.getName().getString(), seq,
                        active ? AnchorConfig.uniqueSourceAnchors(5, seq) : 0, active, "Loaded avatar",
                        reason + " Dimension: " + avatar.level().dimension().location()));
            } else {
                rows.add(new AnchorSourceRow(5, id, "Registered Persona avatar", -1, 0, false,
                        found == null ? "Not available here" : "Invalid entity",
                        "Native avatar registration exists, but no valid loaded avatar is available in your current dimension. No contribution."));
            }
        }
    }

    public static List<AnchorSourceRow> followers(ServerPlayer owner) {
        var data = FollowerData.get(owner.server);
        List<AnchorSourceRow> rows = new ArrayList<>();
        boolean eligible = HonorificPrayerHandler.eligible(owner);
        data.book().snapshot().forEach((id, patron) -> {
            if (!owner.getUUID().equals(patron)) return;
            ServerPlayer online = owner.server.getPlayerList().getPlayer(id);
            if (online != null) HonorificPrayerHandler.refreshSequence(online);
            int sequence = data.book().sequence(id);
            rows.add(new AnchorSourceRow(0, id, name(owner.server, id), sequence,
                    eligible ? AnchorConfig.followerAnchors(sequence) : 0, eligible,
                    online == null ? "Offline" : "Online",
                    !eligible ? "Recipient must be alive, not spectating, and at Sequence 3 to 0."
                            : online == null ? "Bond retained. Last known Sequence." : "Bond accepted. Current Sequence."));
        });
        return sorted(rows);
    }

    /** Shared by the panel and the actual Team/NPC anchor calculation. */
    public static List<AnchorSourceRow> subordinates(ServerPlayer owner) {
        List<AnchorSourceRow> rows = new ArrayList<>();
        boolean redPriest = "red_priest".equalsIgnoreCase(BeyonderData.getPathway(owner));
        boolean ownerActive = owner.isAlive() && !owner.isSpectator() && BeyonderData.isBeyonder(owner);
        boolean leaderEligible = redPriest && ownerActive && TeamUtils.isEligibleLeader(owner);
        if (redPriest && owner.hasData(ModAttachments.TEAM_COMPONENT.get())) {
            TeamComponent team = owner.getData(ModAttachments.TEAM_COMPONENT.get());
            // A copied Team roster on a member must not be presented as their own team.
            if (owner.getStringUUID().equals(team.leaderUUID())) {
                Set<UUID> roster = TeamAnchorRules.validRoster(owner.getUUID(), team.leaderUUID(),
                        team.memberUUIDs(), TeamUtils.getMaxTeamSize(BeyonderData.getSequence(owner)));
                Set<UUID> seen = new HashSet<>();
                for (String raw : team.memberUUIDs()) {
                    UUID id;
                    try { id = UUID.fromString(raw); } catch (IllegalArgumentException | NullPointerException invalid) { continue; }
                    if (id.equals(owner.getUUID()) || !seen.add(id)) continue;
                    ServerPlayer member = owner.server.getPlayerList().getPlayer(id);
                    String memberLeader = member != null && member.hasData(ModAttachments.TEAM_COMPONENT.get())
                            ? member.getData(ModAttachments.TEAM_COMPONENT.get()).leaderUUID() : "";
                    int sequence = member == null ? -1 : BeyonderData.getSequence(member);
                    boolean active = leaderEligible && member != null && member.serverLevel() == owner.serverLevel() && sequence >= 1 && sequence <= 10
                            && TeamAnchorRules.canContribute(owner.getUUID(), roster, id, memberLeader,
                            member.isAlive(), member.isSpectator());
                    String reason = !leaderEligible ? "Leader must be a living Red Priest, not spectating, at Sequence 3 or lower."
                            : !roster.contains(id) ? "Team roster is inconsistent or exceeds capacity."
                            : member == null ? "Offline Team members do not contribute."
                            : member.serverLevel() != owner.serverLevel() ? "Team member is in another dimension."
                            : !member.isAlive() ? "Member is dead."
                            : member.isSpectator() ? "Spectator mode."
                            : !owner.getStringUUID().equals(memberLeader) ? "Member bond does not confirm this leader."
                            : sequence == 0 ? "No Team contribution configured for Sequence 0." : "Valid Team member.";
                    rows.add(new AnchorSourceRow(1, id, name(owner.server, id), sequence,
                            active ? AnchorConfig.subordinateAnchors(sequence) : 0, active,
                            member == null ? "Offline" : "Online", reason));
                }
            }
        }
        Set<UUID> seen = new HashSet<>();
        for (Entity entity : AnchorEntityIndex.candidates(owner.serverLevel(), owner.getUUID())) {
            if (entity.isRemoved() || entity.level() != owner.serverLevel()) continue;
            if (!(entity instanceof LivingEntity npc) || npc instanceof ServerPlayer
                    || npc.getUUID().equals(owner.getUUID()) || !seen.add(npc.getUUID())
                    || !npc.hasData(ModAttachments.SUBORDINATE_COMPONENT.get())) continue;
            SubordinateComponent link = npc.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
            if (!link.isSubordinate() || !owner.getStringUUID().equals(link.getControllerUUID())) continue;
            // A stronger entity link takes priority even when it belongs to another player.
            if (controlledMarionette(npc) || npc instanceof AvatarEntity avatar
                    && avatar.getOriginalOwner() != null
                    && ("error".equalsIgnoreCase(avatar.getPathway()) || "visionary".equalsIgnoreCase(avatar.getPathway()))) continue;
            int sequence = BeyonderData.getSequence(npc);
            boolean active = ownerActive && npc.isAlive() && sequence >= 1 && sequence <= 10;
            String reason = !ownerActive ? "Owner must be a living Beyonder and not spectating."
                    : !npc.isAlive() ? "NPC is dead." : sequence == 0 ? "No NPC contribution configured for Sequence 0."
                    : "Valid subordinate. Dimension: " + entity.level().dimension().location();
            rows.add(new AnchorSourceRow(2, npc.getUUID(), npc.getName().getString(), sequence,
                    active ? AnchorConfig.subordinateAnchors(sequence) : 0, active, "Loaded", reason));
        }
        return sorted(rows);
    }

    private static boolean controlledMarionette(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.hasData(ModAttachments.MARIONETTE_COMPONENT)) return false;
        var link = living.getData(ModAttachments.MARIONETTE_COMPONENT);
        return link.isMarionette() && !link.getControllerUUID().isBlank();
    }
    private static AnchorSourceRow inactive(AnchorSourceRow row, String reason) {
        return new AnchorSourceRow(row.category(), row.id(), row.name(), row.sequence(), 0, false, row.state(), reason);
    }
    private static List<AnchorSourceRow> sorted(List<AnchorSourceRow> rows) {
        rows.sort(AnchorSourceRow.DISPLAY_ORDER);
        return rows;
    }

    private static String name(MinecraftServer server, UUID id) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) return player.getName().getString();
        String remembered = FollowerData.get(server).name(id);
        if (!remembered.isBlank()) return remembered;
        var cache = server.getProfileCache();
        return cache == null ? id.toString() : cache.get(id).map(profile -> profile.getName()).orElse(id.toString());
    }
}

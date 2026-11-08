package com.kawan.lotmanchors.anchor;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.AvatarEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;

/** Shares world enumeration across players; never caches contribution values or validity. */
public final class AnchorEntityIndex {
    private static final ServerTickCache<ServerLevel, OwnedSourceIndex<Entity>> CACHE = new ServerTickCache<>();
    private AnchorEntityIndex() {}

    public static List<Entity> candidates(ServerLevel level, UUID owner) {
        return CACHE.get(level, level.getServer().getTickCount(), () -> build(level)).candidates(owner);
    }

    private static OwnedSourceIndex<Entity> build(ServerLevel level) {
        return OwnedSourceIndex.build(level.getAllEntities(), Entity::getUUID, AnchorEntityIndex::owners);
    }

    private static Collection<UUID> owners(Entity entity) {
        Set<UUID> owners = new HashSet<>();
        if (entity instanceof AvatarEntity avatar && avatar.getOriginalOwner() != null) owners.add(avatar.getOriginalOwner());
        if (entity instanceof LivingEntity living) {
            if (living.hasData(ModAttachments.MARIONETTE_COMPONENT)) {
                var link = living.getData(ModAttachments.MARIONETTE_COMPONENT);
                if (link.isMarionette()) addOwner(owners, link.getControllerUUID());
            }
            if (living.hasData(ModAttachments.SUBORDINATE_COMPONENT.get())) {
                var link = living.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
                if (link.isSubordinate()) addOwner(owners, link.getControllerUUID());
            }
        }
        return owners;
    }

    private static void addOwner(Set<UUID> owners, String raw) {
        if (raw == null) return;
        try {
            UUID id = UUID.fromString(raw);
            if (id.toString().equalsIgnoreCase(raw)) owners.add(id);
        } catch (IllegalArgumentException ignored) { /* Invalid native identity grants nothing. */ }
    }

    public static void invalidate(ServerLevel level) { CACHE.invalidate(level); }
    public static void clear() { CACHE.clear(); }
}

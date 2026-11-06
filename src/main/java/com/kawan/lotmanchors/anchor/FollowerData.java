package com.kawan.lotmanchors.anchor;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.UUID;

public final class FollowerData extends SavedData {
    private final AnchorHistoryBook history = new AnchorHistoryBook();
    public AnchorHistoryBook history() { return history; }
    public void record(UUID owner, String message) { history.add(owner, System.currentTimeMillis(), message); setDirty(); }
    private final FollowerBook book = new FollowerBook();
    private final java.util.Map<UUID, String> names = new java.util.HashMap<>();
    public String name(UUID id) { return names.getOrDefault(id, ""); }
    public void rememberName(UUID id, String name) {
        if (book.patron(id) != null && !name.equals(names.put(id, name))) setDirty();
    }
    public FollowerBook book() { return book; }
    public static FollowerData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(FollowerData::new, FollowerData::load), "lotm_anchors_followers");
    }
    static FollowerData load(CompoundTag tag, HolderLookup.Provider provider) {
        FollowerData data = new FollowerData();
        CompoundTag links = tag.getCompound("followers");
        CompoundTag sequences = tag.getCompound("follower_sequences");
        CompoundTag names = tag.getCompound("follower_names");
        for (String key : links.getAllKeys()) {
            try {
                UUID follower = UUID.fromString(key);
                data.book.restore(follower, UUID.fromString(links.getString(key)));
                if (sequences.contains(key, 3)) data.book.updateSequence(follower, sequences.getInt(key));
                if (data.book.patron(follower) != null) data.names.put(follower, names.getString(key));
            }
            catch (IllegalArgumentException ignored) { /* Invalid entries grant nothing. */ }
        }
        var histories = tag.getCompound("anchor_history");
        for (String key : histories.getAllKeys()) {
            try {
                UUID owner = UUID.fromString(key);
                var list = histories.getList(key, 10);
                for (int i = Math.min(list.size(), AnchorHistoryBook.LIMIT) - 1; i >= 0; i--) {
                    var entry = list.getCompound(i);
                    data.history.add(owner, entry.getLong("time"), entry.getString("message"));
                }
            } catch (IllegalArgumentException ignored) { }
        }
        return data;
    }
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag links = new CompoundTag();
        book.snapshot().forEach((follower, deity) -> links.putString(follower.toString(), deity.toString()));
        tag.put("followers", links);
        CompoundTag sequences = new CompoundTag();
        book.snapshot().keySet().forEach(follower -> sequences.putInt(follower.toString(), book.sequence(follower)));
        tag.put("follower_sequences", sequences);
        CompoundTag names = new CompoundTag();
        book.snapshot().keySet().forEach(follower -> names.putString(follower.toString(), name(follower)));
        tag.put("follower_names", names);
        CompoundTag histories = new CompoundTag();
        for (UUID owner : history.owners()) {
            var list = new net.minecraft.nbt.ListTag();
            for (var entry : history.entries(owner)) {
                var item = new CompoundTag(); item.putLong("time", entry.time()); item.putString("message", entry.message()); list.add(item);
            }
            histories.put(owner.toString(), list);
        }
        tag.put("anchor_history", histories);
        return tag;
    }
}

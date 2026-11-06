package com.kawan.lotmanchors.anchor;

import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

/** Exercises actual SavedData NBT without opening a world or starting a server. */
public final class FollowerDataTest {
    public static void main(String[] args) {
        UUID deity = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID follower = UUID.fromString("00000000-0000-0000-0000-000000000002");
        FollowerData data = new FollowerData();
        UUID offer = data.book().offer(deity, follower, 0, 100);
        UUID invitation = data.book().invite(deity, offer, 1, 100);
        if (!data.book().respond(follower, invitation, true, 2)) throw new AssertionError("consent");
        data.book().updateSequence(follower, 2);
        data.rememberName(follower, "Fiel de teste");
        CompoundTag saved = data.save(new CompoundTag(), null);
        FollowerData restored = FollowerData.load(saved, null);
        if (!deity.equals(restored.book().patron(follower)) || restored.book().count(deity) != 1)
            throw new AssertionError("NBT round trip");
        if (restored.book().request(invitation, 3) != null) throw new AssertionError("persisted request");
        if (restored.book().sequence(follower) != 2 || restored.book().countsBySequence(deity)[2] != 1)
            throw new AssertionError("offline sequence survives restart");
        if (!restored.name(follower).equals("Fiel de teste")) throw new AssertionError("offline name survives restart");
        CompoundTag legacy = saved.copy();
        legacy.remove("follower_sequences");
        if (FollowerData.load(legacy, null).book().sequence(follower) != 10) throw new AssertionError("legacy migration");
        CompoundTag corrupt = saved.copy();
        corrupt.getCompound("follower_sequences").putInt(follower.toString(), -1);
        if (FollowerData.load(corrupt, null).book().sequence(follower) != 10) throw new AssertionError("invalid sequence");
        corrupt.getCompound("follower_sequences").putString(follower.toString(), "invalid");
        if (FollowerData.load(corrupt, null).book().sequence(follower) != 10) throw new AssertionError("invalid sequence type");
        saved.getCompound("followers").putString("invalid-uuid", deity.toString());
        saved.getCompound("followers").putString(deity.toString(), "invalid-uuid");
        if (FollowerData.load(saved, null).book().snapshot().size() != 1) throw new AssertionError("invalid NBT");
        if (!FollowerData.load(new CompoundTag(), null).book().snapshot().isEmpty()) throw new AssertionError("old world");
        restored.book().leave(follower);
        if (FollowerData.load(restored.save(new CompoundTag(), null), null).book().count(deity) != 0)
            throw new AssertionError("revocation persisted");
        var history = data.history();
        for (int i=0;i<40;i++) history.add(deity, i, "Event " + i);
        if (history.entries(deity).size()!=32 || history.entries(deity).getFirst().time()!=39
                || history.entries(deity).getLast().time()!=8) throw new AssertionError("bounded newest-first history");
        var loadedHistory = FollowerData.load(data.save(new CompoundTag(), null), null).history();
        if (!loadedHistory.entries(deity).equals(history.entries(deity))) throw new AssertionError("history NBT round trip");
        if (!history.entries(follower).isEmpty()) throw new AssertionError("owner isolation");
        var baseline = java.util.Map.of(3, 100L);
        var changed = java.util.Map.of(3, 500L);
        if (!history.observe(deity, baseline, 0).isEmpty()) throw new AssertionError("no initial event");
        if (!history.observe(deity, changed, 100).isEmpty()) throw new AssertionError("debounce begins");
        if (!history.observe(deity, baseline, 200).isEmpty()) throw new AssertionError("transient ignored");
        history.observe(deity, changed, 300);
        if (!history.observe(deity, changed, 899).isEmpty()) throw new AssertionError("wait for stable value");
        if (history.observe(deity, changed, 900).size()!=1) throw new AssertionError("settled source change");
        if (!history.observe(deity, changed, 1500).isEmpty()) throw new AssertionError("no repeated event");
        history.forget(deity);
        if (!history.observe(deity, baseline, 1600).isEmpty()) throw new AssertionError("reconnect starts baseline");
        var packet = new com.kawan.lotmanchors.network.AnchorHistoryNetwork.Snapshot(deity, history.entries(deity));
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            com.kawan.lotmanchors.network.AnchorHistoryNetwork.Snapshot.CODEC.encode(buffer, packet);
            if (!packet.equals(com.kawan.lotmanchors.network.AnchorHistoryNetwork.Snapshot.CODEC.decode(buffer))
                    || buffer.readableBytes()!=0) throw new AssertionError("history packet round trip");
        } finally { buffer.release(); }
        var invalid = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            invalid.writeUUID(deity); invalid.writeVarInt(33);
            try { com.kawan.lotmanchors.network.AnchorHistoryNetwork.Snapshot.CODEC.decode(invalid);
                throw new AssertionError("oversized packet accepted");
            } catch (IllegalArgumentException expected) { }
        } finally { invalid.release(); }
        System.out.println("FollowerData and history: 23 checks passed");
    }
}

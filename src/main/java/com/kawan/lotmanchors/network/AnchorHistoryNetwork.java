package com.kawan.lotmanchors.network;
import com.kawan.lotmanchors.anchor.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import java.util.*;
public final class AnchorHistoryNetwork {
    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(Request.TYPE, Request.CODEC, (request, context) -> context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer viewer)
                    || !AnchorSourcesNetwork.canView(viewer.getUUID(), request.owner(), viewer.hasPermissions(2))) return;
            long tick = viewer.server.getTickCount();
            var tag = viewer.getPersistentData();
            if (tag.contains("anchor_history_request") && tick >= tag.getLong("anchor_history_request")
                    && tick - tag.getLong("anchor_history_request") < 10) return;
            tag.putLong("anchor_history_request", tick);
            PacketDistributor.sendToPlayer(viewer, new Snapshot(request.owner(), FollowerData.get(viewer.server).history().entries(request.owner())));
        }));
        registrar.playToClient(Snapshot.TYPE, Snapshot.CODEC, AnchorClientHandlers.history());
    }
    public record Request(UUID owner) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lotm_anchors", "history_request"));
        public static final StreamCodec<FriendlyByteBuf, Request> CODEC = StreamCodec.of((b,v) -> b.writeUUID(v.owner()), b -> new Request(b.readUUID()));
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Snapshot(UUID owner, List<AnchorHistoryBook.Entry> entries) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lotm_anchors", "history_snapshot"));
        public static final StreamCodec<FriendlyByteBuf, Snapshot> CODEC = StreamCodec.of((b,v) -> {
            if (v.entries().size() > AnchorHistoryBook.LIMIT) throw new IllegalArgumentException("Too many history entries");
            b.writeUUID(v.owner()); b.writeVarInt(v.entries().size());
            for (var entry : v.entries()) { b.writeLong(entry.time()); b.writeUtf(entry.message(), 240); }
        }, b -> {
            UUID owner = b.readUUID(); int size = b.readVarInt();
            if (size < 0 || size > AnchorHistoryBook.LIMIT) throw new IllegalArgumentException("Invalid history size");
            List<AnchorHistoryBook.Entry> entries = new ArrayList<>();
            for (int i=0;i<size;i++) entries.add(new AnchorHistoryBook.Entry(b.readLong(), b.readUtf(240)));
            return new Snapshot(owner, List.copyOf(entries));
        });
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}

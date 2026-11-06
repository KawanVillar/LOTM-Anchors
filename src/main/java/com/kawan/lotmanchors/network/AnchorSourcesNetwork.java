package com.kawan.lotmanchors.network;

import com.kawan.lotmanchors.anchor.*;
import com.kawan.lotmanchors.client.AnchorNetworkClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import java.util.*;

public final class AnchorSourcesNetwork {
    public static final UUID NONE = new UUID(0, 0);
    public static final int MAX_ROWS = 256;
    private AnchorSourcesNetwork() {}

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(Snapshot.TYPE, Snapshot.CODEC, AnchorNetworkClient::handleSources);
        registrar.playToServer(Request.TYPE, Request.CODEC, (request, context) -> context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer viewer)) return;
            // Limit expensive world scans. Client requests never carry trusted names or values.
            long now = viewer.server.getTickCount();
            long last = viewer.getPersistentData().getLong("lotm_anchors_panel_tick");
            if (request.removeFollower().equals(NONE) && last > 0 && now >= last && now - last < 10) return;
            viewer.getPersistentData().putLong("lotm_anchors_panel_tick", now);
            ServerPlayer owner = viewer.server.getPlayerList().getPlayer(request.owner());
            if (owner == null || !canView(viewer.getUUID(), owner.getUUID(), viewer.hasPermissions(2))) return;
            if (!request.removeFollower().equals(NONE)) {
                if (!canRemove(viewer.getUUID(), owner.getUUID())) return;
                FollowerData data = FollowerData.get(viewer.server);
                if (!data.book().remove(owner.getUUID(), request.removeFollower())) {
                    viewer.sendSystemMessage(net.minecraft.network.chat.Component.literal("[Anchors] This faith bond no longer exists. Refresh the panel."));
                    return;
                }
                data.setDirty();
                data.record(owner.getUUID(), "Faith bond ended: " + (data.name(request.removeFollower()).isBlank() ? request.removeFollower() : data.name(request.removeFollower())));
                ServerPlayer follower = viewer.server.getPlayerList().getPlayer(request.removeFollower());
                if (follower != null) follower.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "[Anchors] Your faith bond has ended."));
            }
            open(viewer, owner);
        }));
    }

    public static boolean canView(UUID viewer, UUID owner, boolean operator) { return viewer.equals(owner) || operator; }
    public static boolean canRemove(UUID viewer, UUID owner) { return viewer.equals(owner); }

    public static void open(ServerPlayer viewer, ServerPlayer owner) {
        if (!canView(viewer.getUUID(), owner.getUUID(), viewer.hasPermissions(2))) return;
        PacketDistributor.sendToPlayer(viewer, Snapshot.fromRows(owner.getUUID(), owner.getName().getString(),
                java.util.Objects.requireNonNullElse(de.jakob.lotm.util.BeyonderData.getPathway(owner), "none"),
                AnchorManager.getNormalAnchors(owner), AnchorSourceService.all(owner)));
    }
    public record Request(UUID owner, UUID removeFollower) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lotm_anchors", "sources_request"));
        public static final StreamCodec<FriendlyByteBuf, Request> CODEC = StreamCodec.of(
                (buf, value) -> { buf.writeUUID(value.owner); buf.writeUUID(value.removeFollower); },
                buf -> new Request(buf.readUUID(), buf.readUUID()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record CategoryTotal(int category, int count, long total) {}

    public record Snapshot(UUID owner, String name, String pathway, long stability, long normal,
                           List<CategoryTotal> categories, boolean truncated, List<AnchorSourceRow> rows)
            implements CustomPacketPayload {
        public long total(int category) {
            return categories.stream().filter(item -> item.category() == category).mapToLong(CategoryTotal::total).findFirst().orElse(0);
        }
        public int count(int category) {
            return categories.stream().filter(item -> item.category() == category).mapToInt(CategoryTotal::count).findFirst().orElse(0);
        }
        public long panelTotal() { return categories.stream().mapToLong(CategoryTotal::total).sum(); }
        public long followers() { return total(0); }
        public long team() { return total(1); }
        public long npcs() { return total(2); }
        public int followerCount() { return count(0); }
        public int teamCount() { return count(1); }
        public int npcCount() { return count(2); }

        public static Snapshot fromRows(UUID owner, String name, String pathway, long normal, List<AnchorSourceRow> sources) {
            List<AnchorSourceRow> normalized = AnchorSourceRules.normalize(sources);
            List<Integer> allowed = PathwayAnchorRules.panelCategories(pathway, normalized.stream().map(AnchorSourceRow::category).toList());
            List<AnchorSourceRow> all = normalized.stream().filter(row -> allowed.contains(row.category()))
                    .sorted(AnchorSourceRow.DISPLAY_ORDER).toList();
            List<CategoryTotal> totals = new ArrayList<>();
            for (int category : allowed) {
                var entries = all.stream().filter(row -> row.category() == category).toList();
                totals.add(new CategoryTotal(category, entries.size(), entries.stream().mapToLong(AnchorSourceRow::value).sum()));
            }
            long unique = totals.stream().mapToLong(CategoryTotal::total).sum();
            return new Snapshot(owner, name, pathway, normal + unique, normal, List.copyOf(totals),
                    all.size() > MAX_ROWS, all.stream().limit(MAX_ROWS).toList());
        }

        public static final Type<Snapshot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lotm_anchors", "sources_snapshot"));
        public static final StreamCodec<FriendlyByteBuf, Snapshot> CODEC = StreamCodec.of((buf, value) -> {
            buf.writeUUID(value.owner); buf.writeUtf(value.name, 128); buf.writeUtf(value.pathway, 64);
            buf.writeLong(value.stability); buf.writeLong(value.normal);
            buf.writeVarInt(value.categories.size());
            for (var item : value.categories) {
                buf.writeInt(item.category()); buf.writeInt(item.count()); buf.writeLong(item.total());
            }
            buf.writeBoolean(value.truncated); buf.writeVarInt(value.rows.size());
            for (var row : value.rows) {
                buf.writeInt(row.category()); buf.writeUUID(row.id()); buf.writeUtf(shorten(row.name(), 128), 128);
                buf.writeInt(row.sequence()); buf.writeLong(row.value()); buf.writeBoolean(row.active());
                buf.writeUtf(shorten(row.state(), 64), 64); buf.writeUtf(shorten(row.reason(), 256), 256);
            }
        }, buf -> {
            UUID owner = buf.readUUID(); String name = buf.readUtf(128); String pathway = buf.readUtf(64);
            long stability = buf.readLong(), normal = buf.readLong();
            int categoryCount = buf.readVarInt();
            if (categoryCount < 0 || categoryCount > AnchorSourceRow.CATEGORY_COUNT) throw new IllegalArgumentException("Invalid category count");
            List<CategoryTotal> totals = new ArrayList<>();
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < categoryCount; i++) {
                int category = buf.readInt();
                if (!PathwayAnchorRules.supportsCategory(pathway, category) || !seen.add(category)) throw new IllegalArgumentException("Invalid source category");
                totals.add(new CategoryTotal(category, buf.readInt(), buf.readLong()));
            }
            boolean truncated = buf.readBoolean();
            int size = buf.readVarInt();
            if (size < 0 || size > MAX_ROWS) throw new IllegalArgumentException("Invalid sources count");
            List<AnchorSourceRow> rows = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int category = buf.readInt();
                if (!seen.contains(category)) throw new IllegalArgumentException("Invalid source category");
                rows.add(new AnchorSourceRow(category, buf.readUUID(), buf.readUtf(128), buf.readInt(), buf.readLong(),
                        buf.readBoolean(), buf.readUtf(64), buf.readUtf(256)));
            }
            return new Snapshot(owner, name, pathway, stability, normal, List.copyOf(totals), truncated, List.copyOf(rows));
        });
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
        private static String shorten(String text, int max) { return text.length() <= max ? text : text.substring(0, max); }
    }
}
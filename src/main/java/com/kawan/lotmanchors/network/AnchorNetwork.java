package com.kawan.lotmanchors.network;

import com.kawan.lotmanchors.config.AnchorConfig;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.anchor.AnchorData;
import com.kawan.lotmanchors.anchor.AnchorSourceRow;
import com.kawan.lotmanchors.anchor.AnchorSourceService;
import com.kawan.lotmanchors.anchor.PathwayAnchorRules;
import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.client.AnchorNetworkClient;
import com.kawan.lotmanchors.event.AnchorSanityHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AnchorNetwork {

    private static final String PROTOCOL_VERSION = "8";

    private AnchorNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar =
                event.registrar(PROTOCOL_VERSION);
        AnchorSourcesNetwork.register(registrar);
        AnchorHistoryNetwork.register(registrar);

        registrar.playToClient(
                OpenAnchorInfoPayload.TYPE,
                OpenAnchorInfoPayload.STREAM_CODEC,
                AnchorNetworkClient::handleOpenInfo
        );
    }

    public static void openInfoScreen(
            ServerPlayer viewer,
            ServerPlayer target) {

        AnchorData data = target.getData(
                LOTMAnchors.ANCHOR_DATA
        );

        String pathway = BeyonderData.getPathway(target);
        int sequence = BeyonderData.getSequence(target);

        String sequenceName =
                BeyonderData.getSequenceName(
                        pathway,
                        sequence
                );

        long anchors = data.getTotalAnchors();
        long lifetimeEarned = data.getLifetimeEarned();
        long lifetimeLost = data.getLifetimeLost();

        List<AnchorSourceRow> sourceRows =
                AnchorSourceService.all(target);
        long uniqueAnchors = sourceRows.stream().mapToLong(AnchorSourceRow::value).sum();
        long totalForStability = anchors + uniqueAnchors;
        var sanity = AnchorSanityHandler.view(target, totalForStability);
        List<UniqueAnchorGroup> uniqueAnchorGroups = new ArrayList<>();
        for (int category : PathwayAnchorRules.panelCategories(pathway, sourceRows.stream().map(AnchorSourceRow::category).toList())) {
            final int selected = category;
            var active = sourceRows.stream().filter(row -> row.category() == selected && row.active()).toList();
            if (active.isEmpty()) continue;
            List<UniqueAnchorSequenceEntry> sequences = new ArrayList<>();
            for (int seq = 0; seq <= 10; seq++) {
                final int selectedSequence = seq;
                var entries = active.stream().filter(row -> row.sequence() == selectedSequence).toList();
                if (!entries.isEmpty()) sequences.add(new UniqueAnchorSequenceEntry(seq, entries.size(),
                        entries.stream().mapToLong(AnchorSourceRow::value).sum()));
            }
            String label = AnchorSourceRow.label(category);
            if (category == 0) label = "Confirmed followers (" + active.size() + ")";
            uniqueAnchorGroups.add(new UniqueAnchorGroup(label,
                    active.stream().mapToLong(AnchorSourceRow::value).sum(), sequences));
        }
        long required = 0L, missing = 0L;
        double risk = 0.0D;
        if (sequence > 0 && sequence <= 4) {
            required = AnchorManager.requiredForTargetSequence(sequence - 1);
            missing = Math.max(0, required - totalForStability);
            risk = AnchorManager.failureChance(totalForStability, sequence - 1);
        }
        PacketDistributor.sendToPlayer(
                viewer,
                new OpenAnchorInfoPayload(
                        target.getUUID(),
                        target.getName().getString(),
                        pathway,
                        sequence,
                        sequenceName,
                        anchors,
                        lifetimeEarned,
                        lifetimeLost,
                        required,
                        missing,
                        risk,
                        uniqueAnchors,
                        totalForStability,
                        sanity.current(), sanity.ceiling(), sanity.required(), sanity.state(),
                        sequence > 0 && sequence <= 4 && AnchorConfig.anchorSanityEnabled()
                                && AnchorConfig.anchorSanityDropPerSecond() > 0
                                ? com.kawan.lotmanchors.anchor.AnchorSanityRules.ceiling(sanity.nativeMaximum(),
                                    totalForStability, required, AnchorConfig.anchorSanityPenalty(sequence - 1)) : -1,
                        uniqueAnchorGroups
                )
        );
    }

    private static List<UniqueAnchorGroup> readUniqueAnchorGroups(
            FriendlyByteBuf buf) {

        int groupCount = buf.readInt();

        List<UniqueAnchorGroup> groups =
                new ArrayList<>(groupCount);

        for (int i = 0; i < groupCount; i++) {

            String name = buf.readUtf();
            long totalValue = buf.readLong();

            int sequenceCount = buf.readInt();

            List<UniqueAnchorSequenceEntry> sequences =
                    new ArrayList<>(sequenceCount);

            for (int j = 0; j < sequenceCount; j++) {

                int sequence = buf.readInt();
                int count = buf.readInt();
                long value = buf.readLong();

                sequences.add(
                        new UniqueAnchorSequenceEntry(
                                sequence,
                                count,
                                value
                        )
                );
            }

            groups.add(
                    new UniqueAnchorGroup(
                            name,
                            totalValue,
                            sequences
                    )
            );
        }

        return groups;
    }

    public record UniqueAnchorSequenceEntry(
            int sequence,
            int count,
            long value
    ) {
    }

    public record UniqueAnchorGroup(
            String name,
            long totalValue,
            List<UniqueAnchorSequenceEntry> sequences
    ) {
    }

    public record OpenAnchorInfoPayload(
            UUID target,
            String playerName,
            String pathway,
            int sequence,
            String sequenceName,
            long anchors,
            long lifetimeEarned,
            long lifetimeLost,
            long required,
            long missing,
            double risk,
            long uniqueAnchors,
            long totalForStability,
            float sanity,
            float sanityCeiling,
            long currentSequenceRequired,
            String sanityState,
            float nextSanityCeiling,
            List<UniqueAnchorGroup> uniqueAnchorGroups
    ) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<OpenAnchorInfoPayload> TYPE =
                new CustomPacketPayload.Type<>(
                        ResourceLocation.fromNamespaceAndPath(
                                "lotm_anchors",
                                "open_anchor_info"
                        )
                );

        public static final net.minecraft.network.codec.StreamCodec<
                FriendlyByteBuf,
                OpenAnchorInfoPayload
                > STREAM_CODEC =
                net.minecraft.network.codec.StreamCodec.of(
                        (buf, payload) -> {

                            buf.writeUUID(payload.target());
                            buf.writeUtf(payload.playerName());
                            buf.writeUtf(payload.pathway());
                            buf.writeInt(payload.sequence());
                            buf.writeUtf(payload.sequenceName());

                            buf.writeLong(payload.anchors());
                            buf.writeLong(payload.lifetimeEarned());
                            buf.writeLong(payload.lifetimeLost());

                            buf.writeLong(payload.required());
                            buf.writeLong(payload.missing());

                            buf.writeDouble(payload.risk());

                            buf.writeLong(payload.uniqueAnchors());
                            buf.writeLong(payload.totalForStability());
                            buf.writeFloat(payload.sanity()); buf.writeFloat(payload.sanityCeiling());
                            buf.writeLong(payload.currentSequenceRequired()); buf.writeUtf(payload.sanityState(), 128);
                            buf.writeFloat(payload.nextSanityCeiling());

                            buf.writeInt(
                                    payload.uniqueAnchorGroups().size()
                            );

                            for (UniqueAnchorGroup group :
                                    payload.uniqueAnchorGroups()) {

                                buf.writeUtf(group.name());
                                buf.writeLong(group.totalValue());

                                buf.writeInt(
                                        group.sequences().size()
                                );

                                for (UniqueAnchorSequenceEntry entry :
                                        group.sequences()) {

                                    buf.writeInt(entry.sequence());
                                    buf.writeInt(entry.count());
                                    buf.writeLong(entry.value());
                                }
                            }
                        },

                        buf -> new OpenAnchorInfoPayload(
                                buf.readUUID(),
                                buf.readUtf(),
                                buf.readUtf(),
                                buf.readInt(),
                                buf.readUtf(),
                                buf.readLong(),
                                buf.readLong(),
                                buf.readLong(),
                                buf.readLong(),
                                buf.readLong(),
                                buf.readDouble(),
                                buf.readLong(),
                                buf.readLong(),
                                buf.readFloat(), buf.readFloat(), buf.readLong(), buf.readUtf(128), buf.readFloat(),
                                readUniqueAnchorGroups(buf)
                        )
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}

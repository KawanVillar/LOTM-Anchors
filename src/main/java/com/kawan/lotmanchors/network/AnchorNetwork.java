package com.kawan.lotmanchors.network;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.anchor.AnchorData;
import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.anchor.UniqueAnchorManager;
import com.kawan.lotmanchors.config.AnchorConfig;
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

    private static final String PROTOCOL_VERSION = "1";

    private AnchorNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar =
                event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(
                OpenAnchorInfoPayload.TYPE,
                OpenAnchorInfoPayload.STREAM_CODEC,
                com.kawan.lotmanchors.client.AnchorNetworkClient::handleOpenInfo
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

        // Calculate Unique Anchors currently possessed by the player.
        long uniqueAnchors =
                UniqueAnchorManager.getErrorAvatarUniqueAnchors(target);

        // Normal Anchors + Unique Anchors.
        // This value is informational for the GUI for now.
        long totalForStability =
                anchors + uniqueAnchors;

        List<UniqueAnchorGroup> uniqueAnchorGroups =
                new ArrayList<>();

        int[] errorAvatarCounts =
                UniqueAnchorManager.countErrorAvatarsBySequence(target);

        List<UniqueAnchorSequenceEntry> errorAvatarSequences =
                new ArrayList<>();

        for (int seq = 1; seq <= 4; seq++) {

            int count = errorAvatarCounts[seq];

            if (count <= 0) {
                continue;
            }

            long valuePerAvatar;

            switch (seq) {
                case 1 ->
                        valuePerAvatar =
                                AnchorConfig.errorAvatarSequence1Anchors();

                case 2 ->
                        valuePerAvatar =
                                AnchorConfig.errorAvatarSequence2Anchors();

                case 3 ->
                        valuePerAvatar =
                                AnchorConfig.errorAvatarSequence3Anchors();

                case 4 ->
                        valuePerAvatar =
                                AnchorConfig.errorAvatarSequence4Anchors();

                default ->
                        valuePerAvatar = 0L;
            }

            long value =
                    (long) count * valuePerAvatar;

            errorAvatarSequences.add(
                    new UniqueAnchorSequenceEntry(
                            seq,
                            count,
                            value
                    )
            );
        }

        if (!errorAvatarSequences.isEmpty()) {

            uniqueAnchorGroups.add(
                    new UniqueAnchorGroup(
                            "Error Avatars",
                            uniqueAnchors,
                            errorAvatarSequences
                    )
            );
        }

        long required = 0L;
        long missing = 0L;
        double risk = 0.0D;

        if (sequence > 0 && sequence <= 4) {

            required =
                    AnchorManager.requiredForTargetSequence(
                            sequence - 1
                    );

            if (anchors < required) {
                missing = required - anchors;
            }

            risk =
                    AnchorManager.failureChance(
                            target,
                            sequence - 1
                    );
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
                                readUniqueAnchorGroups(buf)
                        )
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}

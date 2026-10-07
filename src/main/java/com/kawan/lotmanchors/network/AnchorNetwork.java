package com.kawan.lotmanchors.network;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.anchor.AnchorData;
import com.kawan.lotmanchors.anchor.AnchorManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
                        risk
                )
        );
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
            double risk
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

                            buf.writeDouble(payload.risk);
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
                                buf.readDouble()
                        )
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
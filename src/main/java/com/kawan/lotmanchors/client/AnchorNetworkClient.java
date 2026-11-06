package com.kawan.lotmanchors.client;

import com.kawan.lotmanchors.network.AnchorNetwork;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AnchorNetworkClient {
    public static void handleSources(com.kawan.lotmanchors.network.AnchorSourcesNetwork.Snapshot payload,
                                     IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            client.setScreen(client.screen instanceof AnchorSourcesScreen current
                    ? current.updated(payload) : new AnchorSourcesScreen(payload));
        });
    }

    public static void handleHistory(com.kawan.lotmanchors.network.AnchorHistoryNetwork.Snapshot payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            // Ignore stale replies after closing Sources or switching inspected players.
            if (client.screen instanceof AnchorSourcesScreen sources && sources.showsOwner(payload.owner())) {
                client.setScreen(new AnchorHistoryScreen(payload, sources));
            }
        });
    }
    private AnchorNetworkClient() {
    }

    public static void handleOpenInfo(
            AnchorNetwork.OpenAnchorInfoPayload payload,
            IPayloadContext context) {

        context.enqueueWork(() -> {

            Minecraft minecraft = Minecraft.getInstance();

            minecraft.setScreen(
                    new AnchorInfoScreen(payload)
            );
        });
    }
}

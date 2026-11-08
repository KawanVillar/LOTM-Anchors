package com.kawan.lotmanchors.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/** Common-side factories: client classes are resolved only when a client receives a packet. */
public final class AnchorClientHandlers {
    private AnchorClientHandlers() {}

    public static IPayloadHandler<AnchorNetwork.OpenAnchorInfoPayload> overview() {
        return (payload, context) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.kawan.lotmanchors.client.AnchorNetworkClient.handleOpenInfo(payload, context);
            }
        };
    }

    public static IPayloadHandler<AnchorSourcesNetwork.Snapshot> sources() {
        return (payload, context) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.kawan.lotmanchors.client.AnchorNetworkClient.handleSources(payload, context);
            }
        };
    }

    public static IPayloadHandler<AnchorHistoryNetwork.Snapshot> history() {
        return (payload, context) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.kawan.lotmanchors.client.AnchorNetworkClient.handleHistory(payload, context);
            }
        };
    }
}

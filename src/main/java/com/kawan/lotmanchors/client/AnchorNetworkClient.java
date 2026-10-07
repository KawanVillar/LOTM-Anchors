package com.kawan.lotmanchors.client;

import com.kawan.lotmanchors.network.AnchorNetwork;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AnchorNetworkClient {

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
package com.kawan.lotmanchors;

import com.kawan.lotmanchors.anchor.AnchorData;
import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.command.AnchorCommand;
import com.kawan.lotmanchors.command.FaithCommand;
import com.kawan.lotmanchors.config.AnchorConfig;
import com.kawan.lotmanchors.event.AdvancementStabilityHandler;
import com.kawan.lotmanchors.event.AnchorDeathHandler;
import com.kawan.lotmanchors.event.AnchorSanityHandler;
import com.kawan.lotmanchors.event.PlayerLifecycleHandler;
import com.kawan.lotmanchors.network.AnchorNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

@Mod(LOTMAnchors.MOD_ID)
public final class LOTMAnchors {
    public static final String MOD_ID = "lotm_anchors";
    public static final String MOD_NAME = "LOTM Anchors";

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    public static final Supplier<AttachmentType<AnchorData>> ANCHOR_DATA = ATTACHMENTS.register(
            "anchor_data", () -> AttachmentType.serializable(AnchorData::new)
                    .copyOnDeath()
                    .build()
    );

    public LOTMAnchors(IEventBus modBus, ModContainer container) {
        ATTACHMENTS.register(modBus);
        AnchorConfig.register(container);

        NeoForge.EVENT_BUS.register(new AdvancementStabilityHandler());
        NeoForge.EVENT_BUS.register(new PlayerLifecycleHandler());
        NeoForge.EVENT_BUS.addListener(AnchorCommand::register);
        NeoForge.EVENT_BUS.addListener(FaithCommand::register);
        NeoForge.EVENT_BUS.register(new AnchorDeathHandler());
        NeoForge.EVENT_BUS.register(new AnchorSanityHandler());
        NeoForge.EVENT_BUS.register(new com.kawan.lotmanchors.event.AnchorHistoryHandler());
        modBus.addListener(AnchorNetwork::register);
    }
}

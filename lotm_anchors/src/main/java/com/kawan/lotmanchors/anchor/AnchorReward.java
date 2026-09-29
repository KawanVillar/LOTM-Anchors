package com.kawan.lotmanchors.anchor;

import net.minecraft.server.level.ServerPlayer;

/** Small reward object intended for future native quest definitions. */
public record AnchorReward(long amount, String source) {
    public AnchorReward {
        if (amount < 0) throw new IllegalArgumentException("Anchor reward cannot be negative");
        if (source == null || source.isBlank()) source = "quest";
    }

    public void grant(ServerPlayer player) {
        AnchorManager.give(player, amount, source);
    }
}

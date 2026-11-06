package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.anchor.FollowerBook;
import com.kawan.lotmanchors.anchor.FollowerData;
import com.kawan.lotmanchors.config.AnchorConfig;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

/** Invoked only after the main mod stores a completed prayer. */
public final class HonorificPrayerHandler {
    private HonorificPrayerHandler() {}
    public static boolean eligible(ServerPlayer player) {
        int sequence = BeyonderData.getSequence(player);
        return player.isAlive() && !player.isSpectator() && BeyonderData.isBeyonder(player)
                && sequence >= 0 && sequence <= 3;
    }
    public static Component button(String label, String command, ChatFormatting color) {
        return Component.literal(label).withStyle(style -> style.withColor(color)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
    }
    public static void onPrayer(ServerPlayer target, ServerPlayer worshipper) {
        if (target.getServer() == null || target.getServer() != worshipper.getServer()
                || target.getUUID().equals(worshipper.getUUID()) || !eligible(target)
                || !worshipper.isAlive() || worshipper.isSpectator()) return;
        FollowerBook book = FollowerData.get(target.getServer()).book();
        if (book.patron(worshipper.getUUID()) != null) return;
        long now = System.currentTimeMillis();
        var data = AnchorManager.get(target);
        String key = "follower_offer:" + worshipper.getStringUUID();
        if (data.isPrayerOnCooldown(key, now)) return;
        UUID offer = book.offer(target.getUUID(), worshipper.getUUID(), now,
                AnchorConfig.followerInviteSeconds() * 1000L);
        if (offer == null) return;
        data.setPrayerCooldown(key, now + AnchorConfig.followerOfferCooldownSeconds() * 1000L);
        target.sendSystemMessage(Component.literal("[Anchors] " + worshipper.getName().getString()
                + " prayed to you. Option expires in " + AnchorConfig.followerInviteSeconds() + "s. ").append(button("[Invite as follower]",
                "/faith invite " + offer, ChatFormatting.LIGHT_PURPLE)));
    }
    public static long count(ServerPlayer target) {
        int[] counts = countsBySequence(target);
        long total = 0;
        for (int count : counts) total += count;
        return total;
    }
    public static long anchors(ServerPlayer target) {
        return FollowerBook.weightedAnchors(countsBySequence(target), AnchorConfig::followerAnchors);
    }
    public static void refreshSequence(ServerPlayer follower) {
        if (follower.getServer() == null) return;
        FollowerData data = FollowerData.get(follower.getServer());
        if (data.book().patron(follower.getUUID()) == null) return;
        data.rememberName(follower.getUUID(), follower.getName().getString());
        int sequence = BeyonderData.isBeyonder(follower) ? BeyonderData.getSequence(follower) : 10;
        int previous = data.book().sequence(follower.getUUID());
        if (data.book().updateSequence(follower.getUUID(), sequence)) {
            data.setDirty();
            if (previous != sequence) data.record(data.book().patron(follower.getUUID()), follower.getName().getString()
                    + " contribution: " + AnchorConfig.followerAnchors(previous) + " -> " + AnchorConfig.followerAnchors(sequence)
                    + " anchors (Sequence updated).");
        }
    }
    public static int[] countsBySequence(ServerPlayer target) {
        if (target.getServer() == null || !eligible(target)) return new int[11];
        FollowerData data = FollowerData.get(target.getServer());
        for (ServerPlayer follower : target.getServer().getPlayerList().getPlayers()) {
            if (target.getUUID().equals(data.book().patron(follower.getUUID()))) refreshSequence(follower);
        }
        return data.book().countsBySequence(target.getUUID());
    }
}

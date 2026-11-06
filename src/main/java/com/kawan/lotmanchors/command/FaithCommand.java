package com.kawan.lotmanchors.command;

import com.kawan.lotmanchors.anchor.FollowerBook;
import com.kawan.lotmanchors.anchor.FollowerData;
import com.kawan.lotmanchors.config.AnchorConfig;
import com.kawan.lotmanchors.event.HonorificPrayerHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import java.util.UUID;

public final class FaithCommand {
    private FaithCommand() {}
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("faith")
                .executes(ctx -> info(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("invite").then(Commands.argument("token", UuidArgument.uuid())
                        .executes(ctx -> invite(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "token")))))
                .then(Commands.literal("accept").then(Commands.argument("token", UuidArgument.uuid())
                        .executes(ctx -> respond(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "token"), true))))
                .then(Commands.literal("decline").then(Commands.argument("token", UuidArgument.uuid())
                        .executes(ctx -> respond(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "token"), false))))
                .then(Commands.literal("leave").executes(ctx -> leave(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("remove").then(Commands.argument("follower", UuidArgument.uuid())
                        .executes(ctx -> remove(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "follower"))))));
    }

    private static int invite(ServerPlayer deity, UUID token) {
        long now = System.currentTimeMillis();
        FollowerBook book = FollowerData.get(deity.server).book();
        var request = book.request(token, now);
        if (request == null || request.stage() != FollowerBook.Stage.OFFER
                || !request.deity().equals(deity.getUUID()) || !HonorificPrayerHandler.eligible(deity)) return invalid(deity);
        ServerPlayer worshipper = deity.server.getPlayerList().getPlayer(request.worshipper());
        if (worshipper == null || !worshipper.isAlive() || worshipper.isSpectator()) return invalid(deity);
        UUID invitation = book.invite(deity.getUUID(), token, now, AnchorConfig.followerInviteSeconds() * 1000L);
        if (invitation == null) return invalid(deity);
        worshipper.sendSystemMessage(Component.literal("[Anchors] " + deity.getName().getString()
                        + " invites you to become a follower. You will count as an anchor only after accepting. Invitation expires in "
                        + AnchorConfig.followerInviteSeconds() + "s. ")
                .append(HonorificPrayerHandler.button("[Accept] ", "/faith accept " + invitation, ChatFormatting.GREEN))
                .append(HonorificPrayerHandler.button("[Decline]", "/faith decline " + invitation, ChatFormatting.RED)));
        deity.sendSystemMessage(Component.literal("[Anchors] Invitation sent. Awaiting consent."));
        return 1;
    }

    private static int respond(ServerPlayer worshipper, UUID token, boolean accept) {
        long now = System.currentTimeMillis();
        FollowerData data = FollowerData.get(worshipper.server);
        var request = data.book().request(token, now);
        if (request == null || request.stage() != FollowerBook.Stage.INVITATION
                || !request.worshipper().equals(worshipper.getUUID())) return invalid(worshipper);
        ServerPlayer deity = worshipper.server.getPlayerList().getPlayer(request.deity());
        if (accept && (deity == null || !HonorificPrayerHandler.eligible(deity)
                || !worshipper.isAlive() || worshipper.isSpectator())) return invalid(worshipper);
        if (!data.book().respond(worshipper.getUUID(), token, accept, now)) return invalid(worshipper);
        if (accept) {
            data.book().updateSequence(worshipper.getUUID(), de.jakob.lotm.util.BeyonderData.isBeyonder(worshipper)
                    ? de.jakob.lotm.util.BeyonderData.getSequence(worshipper) : 10);
            HonorificPrayerHandler.refreshSequence(worshipper);
            data.setDirty();
            data.record(request.deity(), worshipper.getName().getString() + " accepted a faith bond.");
        }
        worshipper.sendSystemMessage(Component.literal(accept
                ? "[Anchors] Faith bond confirmed. Use /faith leave to leave at any time."
                : "[Anchors] Invitation declined. No anchors granted."));
        if (deity != null) deity.sendSystemMessage(Component.literal("[Anchors] " + worshipper.getName().getString()
                + (accept ? " accepted your invitation to become a follower." : " declined your invitation.")));
        return 1;
    }

    private static int leave(ServerPlayer player) {
        FollowerData data = FollowerData.get(player.server);
        UUID patron = data.book().patron(player.getUUID());
        boolean removed = data.book().leave(player.getUUID());
        if (removed) { data.setDirty(); data.record(patron, player.getName().getString() + " left the faith bond."); }
        player.sendSystemMessage(Component.literal(removed ? "[Anchors] You are no longer a follower. Your contribution has been removed."
                : "[Anchors] You have no faith bond. Incoming invitations have been cancelled."));
        if (patron != null) {
            ServerPlayer deity = player.server.getPlayerList().getPlayer(patron);
            if (deity != null) deity.sendSystemMessage(Component.literal("[Anchors] " + player.getName().getString() + " left your faith."));
        }
        return 1;
    }

    private static int remove(ServerPlayer deity, UUID follower) {
        FollowerData data = FollowerData.get(deity.server);
        if (!data.book().remove(deity.getUUID(), follower)) return invalid(deity);
        data.setDirty();
        data.record(deity.getUUID(), "Faith bond ended: " + (data.name(follower).isBlank() ? follower : data.name(follower)));
        deity.sendSystemMessage(Component.literal("[Anchors] Follower removed."));
        ServerPlayer player = deity.server.getPlayerList().getPlayer(follower);
        if (player != null) player.sendSystemMessage(Component.literal("[Anchors] Your faith bond has ended."));
        return 1;
    }

    private static int info(ServerPlayer player) {
        var book = FollowerData.get(player.server).book();
        player.sendSystemMessage(Component.literal("[Anchors] Confirmed followers: " + book.count(player.getUUID())
                + ". Your faith bond: " + (book.patron(player.getUUID()) == null ? "none" : book.patron(player.getUUID()))));
        book.snapshot().forEach((follower, deity) -> {
            if (deity.equals(player.getUUID())) {
                ServerPlayer online = player.server.getPlayerList().getPlayer(follower);
                if (online != null) HonorificPrayerHandler.refreshSequence(online);
                int sequence = book.sequence(follower);
                player.sendSystemMessage(Component.literal("- " + (online == null ? follower.toString() : online.getName().getString())
                                + (sequence == 10 ? " (Non-Beyonder)" : " (Sequence " + sequence + ")")
                                + " +" + AnchorConfig.followerAnchors(sequence) + " anchors ")
                        .append(HonorificPrayerHandler.button("[Remove]", "/faith remove " + follower, ChatFormatting.RED)));
            }
        });
        return 1;
    }

    private static int invalid(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("[Anchors] Request invalid, expired, or unavailable. Nothing was granted."));
        return 0;
    }
}

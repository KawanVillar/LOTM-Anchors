package com.kawan.lotmanchors.command;

import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.network.AnchorNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.kawan.lotmanchors.anchor.UniqueAnchorManager;

public final class AnchorCommand {

    private AnchorCommand() {}

    public static void register(RegisterCommandsEvent event) {

        CommandDispatcher<CommandSourceStack> d =
                event.getDispatcher();

        d.register(
                Commands.literal("anchors")

                        // /anchors
                        .executes(ctx ->
                                openInfo(
                                        ctx.getSource(),
                                        ctx.getSource().getPlayerOrException()
                                )
                        )

                        // /anchors info
                        .then(
                                Commands.literal("info")
                                        .executes(ctx ->
                                                openInfo(
                                                        ctx.getSource(),
                                                        ctx.getSource().getPlayerOrException()
                                                )
                                        )

                                        // /anchors info <player>
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )
                                                        .requires(s -> s.hasPermission(2))
                                                        .executes(ctx ->
                                                                openInfo(
                                                                        ctx.getSource(),
                                                                        EntityArgument.getPlayer(
                                                                                ctx,
                                                                                "player"
                                                                        )
                                                                )
                                                        )
                                        )
                        )
                        // /anchors debug avatars
                        // /anchors debug avatars
                        .then(
                                Commands.literal("debug")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.literal("avatars")
                                                        .executes(ctx -> {
                                                            ServerPlayer player =
                                                                    ctx.getSource().getPlayerOrException();

                                                            int[] counts =
                                                                    UniqueAnchorManager.countErrorAvatarsBySequence(player);

                                                            long uniqueAnchors =
                                                                    UniqueAnchorManager.getErrorAvatarUniqueAnchors(player);

                                                            player.sendSystemMessage(
                                                                    net.minecraft.network.chat.Component.literal(
                                                                            "§e[LOTM Anchors] Error Avatar Debug"
                                                                    )
                                                            );

                                                            player.sendSystemMessage(
                                                                    net.minecraft.network.chat.Component.literal(
                                                                            "§7Sequence 1: §f"
                                                                                    + counts[1]
                                                                                    + " §7× 10,000 = §f"
                                                                                    + ((long) counts[1] * 10_000L)
                                                                    )
                                                            );

                                                            player.sendSystemMessage(
                                                                    net.minecraft.network.chat.Component.literal(
                                                                            "§7Sequence 2: §f"
                                                                                    + counts[2]
                                                                                    + " §7× 2,500 = §f"
                                                                                    + ((long) counts[2] * 2_500L)
                                                                    )
                                                            );

                                                            player.sendSystemMessage(
                                                                    net.minecraft.network.chat.Component.literal(
                                                                            "§7Sequence 3: §f"
                                                                                    + counts[3]
                                                                                    + " §7× 500 = §f"
                                                                                    + ((long) counts[3] * 500L)
                                                                    )
                                                            );

                                                            player.sendSystemMessage(
                                                                    net.minecraft.network.chat.Component.literal(
                                                                            "§7Sequence 4: §f"
                                                                                    + counts[4]
                                                                                    + " §7× 100 = §f"
                                                                                    + ((long) counts[4] * 100L)
                                                                    )
                                                            );

                                                            player.sendSystemMessage(
                                                                    net.minecraft.network.chat.Component.literal(
                                                                            "§aTotal Unique Anchors: §f"
                                                                                    + uniqueAnchors
                                                                    )
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )

                        // /anchors give
                        .then(
                                Commands.literal("give")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                LongArgumentType.longArg(1)
                                                                        )
                                                                        .executes(ctx -> {

                                                                            ServerPlayer player =
                                                                                    EntityArgument.getPlayer(
                                                                                            ctx,
                                                                                            "player"
                                                                                    );

                                                                            long amount =
                                                                                    LongArgumentType.getLong(
                                                                                            ctx,
                                                                                            "amount"
                                                                                    );

                                                                            AnchorManager.give(
                                                                                    player,
                                                                                    amount,
                                                                                    "command"
                                                                            );

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )

                        // /anchors take
                        .then(
                                Commands.literal("take")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                LongArgumentType.longArg(1)
                                                                        )
                                                                        .executes(ctx -> {

                                                                            ServerPlayer player =
                                                                                    EntityArgument.getPlayer(
                                                                                            ctx,
                                                                                            "player"
                                                                                    );

                                                                            long amount =
                                                                                    LongArgumentType.getLong(
                                                                                            ctx,
                                                                                            "amount"
                                                                                    );

                                                                            AnchorManager.take(
                                                                                    player,
                                                                                    amount,
                                                                                    "command"
                                                                            );

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )

                        // /anchors set
                        .then(
                                Commands.literal("set")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                LongArgumentType.longArg(0)
                                                                        )
                                                                        .executes(ctx -> {

                                                                            ServerPlayer player =
                                                                                    EntityArgument.getPlayer(
                                                                                            ctx,
                                                                                            "player"
                                                                                    );

                                                                            long amount =
                                                                                    LongArgumentType.getLong(
                                                                                            ctx,
                                                                                            "amount"
                                                                                    );

                                                                            AnchorManager.set(
                                                                                    player,
                                                                                    amount
                                                                            );

                                                                            player.sendSystemMessage(
                                                                                    net.minecraft.network.chat.Component.literal(
                                                                                            "§e[Anchors] Set to "
                                                                                                    + amount
                                                                                                    + "."
                                                                                    )
                                                                            );

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
        );
    }

    private static int openInfo(
            CommandSourceStack source,
            ServerPlayer target) {

        ServerPlayer viewer;

        try {
            viewer = source.getPlayerOrException();
        } catch (Exception e) {
            return 0;
        }

        AnchorNetwork.openInfoScreen(viewer, target);

        return 1;
    }
}
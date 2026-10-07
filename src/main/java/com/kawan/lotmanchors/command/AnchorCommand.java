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
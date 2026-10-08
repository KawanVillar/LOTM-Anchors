package com.kawan.lotmanchors.command;

import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.network.AnchorNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class AnchorCommand {

    private AnchorCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher =
                event.getDispatcher();

        dispatcher.register(
                Commands.literal("anchors")
                        .executes(ctx -> openInfo(
                                ctx.getSource(),
                                ctx.getSource().getPlayerOrException()
                        ))
                        .then(Commands.literal("info")
                                .executes(ctx -> openInfo(
                                        ctx.getSource(),
                                        ctx.getSource().getPlayerOrException()
                                ))
                                .then(Commands.argument(
                                                        "player",
                                                        EntityArgument.player()
                                                )
                                                .requires(source -> source.hasPermission(2))
                                                .executes(ctx -> openInfo(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player")
                                                ))
                                )
                        )
                        .then(Commands.literal("give")
                                .requires(source -> source.hasPermission(2))
                                .then(playerAmountArgument(
                                        "amount",
                                        (player, amount) ->
                                                AnchorManager.give(
                                                        player,
                                                        amount,
                                                        "command"
                                                )
                                ))
                        )
                        .then(Commands.literal("take")
                                .requires(source -> source.hasPermission(2))
                                .then(playerAmountArgument(
                                        "amount",
                                        (player, amount) ->
                                                AnchorManager.take(
                                                        player,
                                                        amount,
                                                        "command"
                                                )
                                ))
                        )
                        .then(Commands.literal("set")
                                .requires(source -> source.hasPermission(2))
                                .then(playerAmountArgument(
                                        "amount",
                                        (player, amount) -> {
                                            AnchorManager.set(player, amount);

                                            player.sendSystemMessage(
                                                    Component.literal(
                                                            "§e[Anchors] Set to "
                                                                    + amount + "."
                                                    )
                                            );
                                        },
                                        0
                                ))
                        )
        );
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> playerAmountArgument(
            String amountName,
            AnchorAction action
    ) {
        return playerAmountArgument(amountName, action, 1);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> playerAmountArgument(
            String amountName,
            AnchorAction action,
            long minimum
    ) {
        return Commands.argument("player", EntityArgument.player())
                .then(Commands.argument(
                                        amountName,
                                        LongArgumentType.longArg(minimum)
                                )
                                .executes(ctx -> {
                                    ServerPlayer player =
                                            EntityArgument.getPlayer(ctx, "player");

                                    long amount =
                                            LongArgumentType.getLong(ctx, amountName);

                                    action.execute(player, amount);
                                    return 1;
                                })
                );
    }

    private static int openInfo(
            CommandSourceStack source,
            ServerPlayer target
    ) {
        try {
            ServerPlayer viewer = source.getPlayerOrException();
            AnchorNetwork.openInfoScreen(viewer, target);
            return 1;
        } catch (Exception ignored) {
            return 0;
        }
    }

    @FunctionalInterface
    private interface AnchorAction {
        void execute(ServerPlayer player, long amount);
    }
}
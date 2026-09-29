package com.kawan.lotmanchors.command;

import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.config.AnchorConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class AnchorCommand {
    private AnchorCommand() {}

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("anchors")
                .executes(ctx -> info(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("give").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(ctx -> {
                                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            AnchorManager.give(p, amount, "comando");
                                            return 1;
                                        }))))
                .then(Commands.literal("take").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(ctx -> {
                                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            AnchorManager.take(p, amount, "comando");
                                            return 1;
                                        }))))
                .then(Commands.literal("set").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(ctx -> {
                                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            AnchorManager.set(p, amount);
                                            p.sendSystemMessage(Component.literal("§e[Âncoras] Definidas para " + amount + "."));
                                            return 1;
                                        }))))
                .then(Commands.literal("check")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> info(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))));
    }

    private static int info(CommandSourceStack source, ServerPlayer player) {
        String pathway = de.jakob.lotm.util.BeyonderData.getPathway(player);
        int sequence = de.jakob.lotm.util.BeyonderData.getSequence(player);
        long anchors = AnchorManager.getAnchors(player);
        long required = AnchorManager.requiredForNextAdvance(sequence);
        double risk = AnchorManager.failureChance(player, sequence - 1);
        source.sendSuccess(() -> Component.literal(
                "§d[LOTM Anchors] §f" + player.getGameProfile().name()
                        + " §7| Pathway: §f" + pathway
                        + " §7| Seq: §f" + sequence
                        + " §7| Âncoras: §f" + anchors
                        + " §7| Próximo requisito: §f" + required
                        + " §7| Risco adicional: §f" + Math.round(risk * 100.0D) + "%"), false);
        return 1;
    }
}

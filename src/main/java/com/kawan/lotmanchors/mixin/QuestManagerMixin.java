package com.kawan.lotmanchors.mixin;

import com.kawan.lotmanchors.event.QuestAnchorHandler;

import de.jakob.lotm.beyonders.quest.QuestManager;

import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuestManager.class)
public abstract class QuestManagerMixin {

    @Inject(method = "completeQuest", at = @At("TAIL"))
    private static void lotmAnchors$onQuestCompleted(
            ServerPlayer player,
            String questId,
            CallbackInfo ci) {

        QuestAnchorHandler.rewardCompletedQuestFromMixin(player, questId);
    }
}
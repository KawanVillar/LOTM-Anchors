package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.anchor.AnchorData;
import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.config.AnchorConfig;

import de.jakob.lotm.beyonders.quest.Quest;
import de.jakob.lotm.beyonders.quest.QuestRegistry;

import net.minecraft.server.level.ServerPlayer;

public final class QuestAnchorHandler {

    private static final String DEFEND_VILLAGE = "defend_village";

    public static void rewardCompletedQuestFromMixin(
            ServerPlayer player,
            String questId) {

        AnchorData anchors =
                player.getData(LOTMAnchors.ANCHOR_DATA);

        Quest quest =
                QuestRegistry.getQuest(questId);

        if (quest == null) {
            return;
        }

        boolean repeat =
                anchors.hasQuestReward(questId);

        boolean fixedReward =
                DEFEND_VILLAGE.equals(questId);

        long baseReward =
                AnchorConfig.questReward(questId);

        long reward;

        if (!repeat || fixedReward) {
            reward = baseReward;
        } else {
            reward = Math.round(
                    baseReward *
                            AnchorConfig.questRepeatMultiplier()
            );
        }

        if (reward <= 0) {
            return;
        }

        AnchorManager.give(
                player,
                reward,
                "Quest LOTMCraft: " + questId
        );

        if (!repeat) {
            anchors.markQuestRewarded(questId);
        }
    }
}
package com.kawan.lotmanchors.event;

import com.kawan.lotmanchors.LOTMAnchors;
import com.kawan.lotmanchors.anchor.AnchorData;
import com.kawan.lotmanchors.anchor.AnchorManager;
import com.kawan.lotmanchors.config.AnchorConfig;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.beyonders.quest.Quest;
import de.jakob.lotm.beyonders.quest.QuestRegistry;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class QuestAnchorHandler {

    private static final String DEFEND_VILLAGE = "defend_village";

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        QuestComponent quests =
                player.getData(ModAttachments.QUEST_COMPONENT);

        AnchorData anchors =
                player.getData(LOTMAnchors.ANCHOR_DATA);

        String currentQuest = "";
        float currentProgress = -1.0F;

        if (!quests.getQuestProgress().isEmpty()) {

            currentQuest =
                    quests.getQuestProgress()
                            .keySet()
                            .iterator()
                            .next();

            Float progress =
                    quests.getQuestProgress()
                            .get(currentQuest);

            currentProgress =
                    progress == null
                            ? 0.0F
                            : progress;
        }

        String previousQuest =
                anchors.getActiveQuest();

        float previousProgress =
                anchors.getActiveQuestProgress();

        boolean questFinished =
                !previousQuest.isEmpty()
                        && currentQuest.isEmpty()
                        && quests.getCompletedQuests()
                        .contains(previousQuest);

        if (questFinished) {

            rewardCompletedQuest(
                    player,
                    anchors,
                    previousQuest
            );

            anchors.clearActiveQuest();

            return;
        }

        if (!currentQuest.isEmpty()) {

            anchors.setActiveQuest(
                    currentQuest,
                    currentProgress
            );

        } else if (previousQuest.isEmpty()) {

            anchors.clearActiveQuest();
        }
    }

    private void rewardCompletedQuest(
            ServerPlayer player,
            AnchorData anchors,
            String questId
    ) {

        Quest quest = QuestRegistry.getQuest(questId);

        if (quest == null) {
            return;
        }

        boolean repeat = anchors.hasQuestReward(questId);

        boolean fixedReward = DEFEND_VILLAGE.equals(questId);

        long baseReward = AnchorConfig.questReward(questId);

        long reward;

        if (!repeat || fixedReward) {

            reward = baseReward;

        } else {

            reward = Math.round(
                    baseReward
                            * AnchorConfig.questRepeatMultiplier()
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

            anchors.markQuestRewarded(
                    questId
            );
        }
    }
}
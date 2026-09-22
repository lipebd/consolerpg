package rpg.quests.rewards;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Stores material quest rewards in stable quest and reward order.
public final class QuestRewardCatalog {

    private final Map<String, List<QuestRewardDefinition>> rewardsByQuestId;

    public QuestRewardCatalog() {
        rewardsByQuestId = new LinkedHashMap<>();
    }

    public void register(QuestRewardDefinition rewardDefinition) {

        if (rewardDefinition == null) {
            throw new IllegalArgumentException("Quest reward definition cannot be null.");
        }

        String questId = rewardDefinition.getQuestId();

        List<QuestRewardDefinition> questRewards =
                rewardsByQuestId.computeIfAbsent(questId, ignored -> new ArrayList<>());

        int expectedOrder = questRewards.size() + 1;

        if (rewardDefinition.getOrder() != expectedOrder) {
            throw new IllegalArgumentException(
                    "Quest reward order must start at 1 and be consecutive for "
                            + questId
                            + ". Expected "
                            + expectedOrder
                            + " but found "
                            + rewardDefinition.getOrder()
                            + ".");
        }

        String rewardKey = createRewardKey(rewardDefinition);

        Set<String> existingRewardKeys = new LinkedHashSet<>();

        for (QuestRewardDefinition existingReward : questRewards) {

            existingRewardKeys.add(createRewardKey(existingReward));
        }

        if (!existingRewardKeys.add(rewardKey)) {
            throw new IllegalArgumentException(
                    "Quest cannot repeat reward type and ID: " + questId + " -> " + rewardKey);
        }

        questRewards.add(rewardDefinition);
    }

    public List<QuestRewardDefinition> getByQuestId(String questId) {

        if (questId == null || questId.isBlank()) {
            throw new IllegalArgumentException("Quest ID cannot be empty.");
        }

        List<QuestRewardDefinition> rewards = rewardsByQuestId.get(questId);

        if (rewards == null) {
            return List.of();
        }

        return List.copyOf(rewards);
    }

    public List<QuestRewardDefinition> getAll() {
        List<QuestRewardDefinition> allRewards = new ArrayList<>();

        for (List<QuestRewardDefinition> questRewards : rewardsByQuestId.values()) {

            allRewards.addAll(questRewards);
        }

        return List.copyOf(allRewards);
    }

    private String createRewardKey(QuestRewardDefinition rewardDefinition) {

        return rewardDefinition.getRewardType() + ":" + rewardDefinition.getRewardId().orElse("");
    }
}

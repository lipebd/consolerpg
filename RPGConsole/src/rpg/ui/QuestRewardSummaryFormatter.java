package rpg.ui;

import java.util.ArrayList;
import java.util.List;

import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.quests.rewards.QuestRewardDefinition;

// Derives player-facing quest reward summaries from canonical data.
public final class QuestRewardSummaryFormatter {

    private final ItemCatalog itemCatalog;

    public QuestRewardSummaryFormatter(ItemCatalog itemCatalog) {

        if (itemCatalog == null) {
            throw new IllegalArgumentException("Item catalog cannot be null.");
        }

        this.itemCatalog = itemCatalog;
    }

    public List<String> format(List<QuestRewardDefinition> rewards) {

        if (rewards == null) {
            throw new IllegalArgumentException("Quest rewards cannot be null.");
        }

        List<String> summaries = new ArrayList<>();

        for (QuestRewardDefinition reward : rewards) {
            if (reward == null) {
                throw new IllegalArgumentException("Quest rewards cannot contain null entries.");
            }

            summaries.add(formatReward(reward));
        }

        return List.copyOf(summaries);
    }

    private String formatReward(QuestRewardDefinition reward) {

        return switch (reward.getRewardType()) {
            case GOLD -> reward.getQuantity() + " GOLD";

            case PARTY_EXP -> reward.getQuantity() + " EXP para cada integrante";

            case ITEM -> formatItemReward(reward);
        };
    }

    private String formatItemReward(QuestRewardDefinition reward) {

        String itemId =
                reward.getRewardId()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "ITEM quest reward has no item ID."));

        Item item = itemCatalog.getById(itemId);

        return reward.getQuantity() + "x " + item.getName();
    }
}

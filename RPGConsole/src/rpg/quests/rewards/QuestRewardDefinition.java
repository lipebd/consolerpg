package rpg.quests.rewards;

import java.util.Optional;

import rpg.characters.Party;
import rpg.items.Inventory;

// Stores one immutable material quest reward.
public final class QuestRewardDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String questId;
    private final int order;
    private final QuestRewardType rewardType;
    private final String rewardId;
    private final int quantity;

    public QuestRewardDefinition(
            String questId, int order, QuestRewardType rewardType, String rewardId, int quantity) {

        this.questId = validateId(questId, "Quest");

        if (order <= 0) {
            throw new IllegalArgumentException("Quest reward order must be positive.");
        }

        if (rewardType == null) {
            throw new IllegalArgumentException("Quest reward type cannot be null.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quest reward quantity must be positive.");
        }

        this.order = order;
        this.rewardType = rewardType;
        this.rewardId = validateRewardId(rewardType, rewardId);

        validateQuantity(rewardType, quantity);

        this.quantity = quantity;
    }

    public String getQuestId() {
        return questId;
    }

    public int getOrder() {
        return order;
    }

    public QuestRewardType getRewardType() {
        return rewardType;
    }

    public Optional<String> getRewardId() {
        return Optional.ofNullable(rewardId);
    }

    public int getQuantity() {
        return quantity;
    }

    private static String validateRewardId(QuestRewardType rewardType, String rewardId) {

        if (rewardType == QuestRewardType.ITEM) {
            if (rewardId == null || rewardId.isBlank()) {
                throw new IllegalArgumentException("ITEM quest reward requires an item ID.");
            }

            return validateId(rewardId, "Quest reward item");
        }

        if (rewardId != null) {
            throw new IllegalArgumentException(
                    rewardType + " quest reward cannot define a reward ID.");
        }

        return null;
    }

    private static void validateQuantity(QuestRewardType rewardType, int quantity) {

        switch (rewardType) {
            case GOLD -> {
                if (quantity > Party.MAXIMUM_GOLD) {
                    throw new IllegalArgumentException(
                            "GOLD quest reward cannot exceed "
                                    + Party.MAXIMUM_GOLD
                                    + ": "
                                    + quantity);
                }
            }

            case ITEM -> {
                if (quantity > Inventory.MAXIMUM_QUANTITY_PER_ITEM) {

                    throw new IllegalArgumentException(
                            "ITEM quest reward quantity cannot exceed "
                                    + Inventory.MAXIMUM_QUANTITY_PER_ITEM
                                    + ": "
                                    + quantity);
                }
            }

            case PARTY_EXP -> {
                // Positive int values are supported by character progression.
            }
        }
    }

    private static String validateId(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    fieldName
                            + " ID must use lowercase letters, numbers and single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }
}

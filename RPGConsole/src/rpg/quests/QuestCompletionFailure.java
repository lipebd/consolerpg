package rpg.quests;

import java.util.Optional;

// Describes one correctable reason why quest completion could not proceed.
public final class QuestCompletionFailure {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String questId;
    private final QuestCompletionFailureType failureType;
    private final Optional<String> targetId;
    private final int requiredQuantity;
    private final int availableQuantity;

    private QuestCompletionFailure(
            String questId,
            QuestCompletionFailureType failureType,
            Optional<String> targetId,
            int requiredQuantity,
            int availableQuantity) {
        this.questId = validateId(questId, "Quest");

        if (failureType == null) {
            throw new IllegalArgumentException("Quest completion failure type cannot be null.");
        }

        if (targetId == null) {
            throw new IllegalArgumentException("Quest completion failure target cannot be null.");
        }

        if (requiredQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Quest completion failure required quantity must be positive.");
        }

        if (availableQuantity < 0 || availableQuantity >= requiredQuantity) {
            throw new IllegalArgumentException(
                    "Quest completion failure available quantity must be "
                            + "nonnegative and lower than the required quantity.");
        }

        this.failureType = failureType;
        this.targetId = targetId.map(value -> validateId(value, "Quest completion failure target"));
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;

        validateTarget();
    }

    static QuestCompletionFailure missingDeliveryItem(
            String questId, String itemId, int requiredQuantity, int ownedQuantity) {
        return new QuestCompletionFailure(
                questId,
                QuestCompletionFailureType.MISSING_DELIVERY_ITEM,
                Optional.of(itemId),
                requiredQuantity,
                ownedQuantity);
    }

    static QuestCompletionFailure goldCapacityExceeded(
            String questId, int rewardQuantity, int remainingCapacity) {
        return new QuestCompletionFailure(
                questId,
                QuestCompletionFailureType.GOLD_CAPACITY_EXCEEDED,
                Optional.empty(),
                rewardQuantity,
                remainingCapacity);
    }

    static QuestCompletionFailure itemCapacityExceeded(
            String questId, String itemId, int rewardQuantity, int remainingCapacity) {
        return new QuestCompletionFailure(
                questId,
                QuestCompletionFailureType.ITEM_CAPACITY_EXCEEDED,
                Optional.of(itemId),
                rewardQuantity,
                remainingCapacity);
    }

    private void validateTarget() {
        switch (failureType) {
            case MISSING_DELIVERY_ITEM:
            case ITEM_CAPACITY_EXCEEDED:
                if (targetId.isEmpty()) {
                    throw new IllegalArgumentException(failureType + " must define an item ID.");
                }

                break;

            case GOLD_CAPACITY_EXCEEDED:
                if (targetId.isPresent()) {
                    throw new IllegalArgumentException(
                            "GOLD_CAPACITY_EXCEEDED cannot define a target ID.");
                }

                break;
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

    public String getQuestId() {
        return questId;
    }

    public QuestCompletionFailureType getFailureType() {
        return failureType;
    }

    public Optional<String> getTargetId() {
        return targetId;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}

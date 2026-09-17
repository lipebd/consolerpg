package rpg.quests;

import java.util.Optional;

// Describes one ordered quest-progress change for later presentation.
public final class QuestProgressUpdate {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final QuestProgressUpdateType updateType;
    private final String questId;
    private final int step;
    private final Optional<String> objectiveId;
    private final int currentQuantity;
    private final int requiredQuantity;

    private QuestProgressUpdate(
            QuestProgressUpdateType updateType,
            String questId,
            int step,
            Optional<String> objectiveId,
            int currentQuantity,
            int requiredQuantity) {
        if (updateType == null) {
            throw new IllegalArgumentException("Quest progress update type cannot be null.");
        }

        this.updateType = updateType;
        this.questId = validateId(questId, "Quest");
        this.step = validateStep(step);

        if (objectiveId == null) {
            throw new IllegalArgumentException("Quest progress update objective cannot be null.");
        }

        this.objectiveId = objectiveId.map(value -> validateId(value, "Quest objective"));

        validateQuantities(updateType, this.objectiveId, currentQuantity, requiredQuantity);

        this.currentQuantity = currentQuantity;
        this.requiredQuantity = requiredQuantity;
    }

    static QuestProgressUpdate objectiveProgress(
            String questId,
            int step,
            String objectiveId,
            int currentQuantity,
            int requiredQuantity) {
        return new QuestProgressUpdate(
                QuestProgressUpdateType.OBJECTIVE_PROGRESS,
                questId,
                step,
                Optional.of(objectiveId),
                currentQuantity,
                requiredQuantity);
    }

    static QuestProgressUpdate objectiveCompleted(
            String questId, int step, String objectiveId, int requiredQuantity) {
        return new QuestProgressUpdate(
                QuestProgressUpdateType.OBJECTIVE_COMPLETED,
                questId,
                step,
                Optional.of(objectiveId),
                requiredQuantity,
                requiredQuantity);
    }

    static QuestProgressUpdate stepCompleted(String questId, int step) {
        return new QuestProgressUpdate(
                QuestProgressUpdateType.STEP_COMPLETED, questId, step, Optional.empty(), 0, 0);
    }

    static QuestProgressUpdate questCompleted(String questId, int step) {
        return new QuestProgressUpdate(
                QuestProgressUpdateType.QUEST_COMPLETED, questId, step, Optional.empty(), 0, 0);
    }

    private static void validateQuantities(
            QuestProgressUpdateType updateType,
            Optional<String> objectiveId,
            int currentQuantity,
            int requiredQuantity) {
        switch (updateType) {
            case OBJECTIVE_PROGRESS:
                if (objectiveId.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Objective progress update must define an objective ID.");
                }

                if (requiredQuantity <= 0
                        || currentQuantity <= 0
                        || currentQuantity >= requiredQuantity) {
                    throw new IllegalArgumentException(
                            "Objective progress update requires progress "
                                    + "between zero and the required quantity.");
                }

                break;

            case OBJECTIVE_COMPLETED:
                if (objectiveId.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Objective completion update must define an objective ID.");
                }

                if (requiredQuantity <= 0 || currentQuantity != requiredQuantity) {
                    throw new IllegalArgumentException(
                            "Objective completion update must equal the " + "required quantity.");
                }

                break;

            case STEP_COMPLETED:
            case QUEST_COMPLETED:
                if (objectiveId.isPresent()) {
                    throw new IllegalArgumentException(
                            updateType + " update cannot define an objective ID.");
                }

                if (currentQuantity != 0 || requiredQuantity != 0) {
                    throw new IllegalArgumentException(
                            updateType + " update cannot define objective quantities.");
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
                            + " ID must use lowercase letters, numbers and "
                            + "single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }

    private static int validateStep(int step) {
        if (step <= 0) {
            throw new IllegalArgumentException("Quest progress update step must be positive.");
        }

        return step;
    }

    public QuestProgressUpdateType getUpdateType() {
        return updateType;
    }

    public String getQuestId() {
        return questId;
    }

    public int getStep() {
        return step;
    }

    public Optional<String> getObjectiveId() {
        return objectiveId;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }
}

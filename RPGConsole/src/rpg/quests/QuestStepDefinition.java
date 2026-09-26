package rpg.quests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Stores the immutable data of one sequential quest step.
public final class QuestStepDefinition {

    private final int step;
    private final String title;
    private final String description;
    private final List<QuestObjectiveDefinition> objectives;

    public QuestStepDefinition(
            int step, String title, String description, List<QuestObjectiveDefinition> objectives) {
        if (step <= 0) {
            throw new IllegalArgumentException("Quest step number must be positive.");
        }

        this.step = step;
        this.title = validateTitle(title);
        this.description = validateText(description, "Quest step description");

        validateObjectives(objectives);
        this.objectives = List.copyOf(objectives);
    }

    private static void validateObjectives(List<QuestObjectiveDefinition> objectives) {
        if (objectives == null || objectives.isEmpty()) {
            throw new IllegalArgumentException("Quest step must contain at least one objective.");
        }

        Set<String> objectiveIds = new HashSet<>();
        Set<String> objectiveTargets = new HashSet<>();
        int talkObjectiveCount = 0;
        boolean hasDeliveryObjective = false;

        for (int index = 0; index < objectives.size(); index++) {
            QuestObjectiveDefinition objective = objectives.get(index);

            if (objective == null) {
                throw new IllegalArgumentException("Quest objectives cannot contain null entries.");
            }

            int expectedOrder = index + 1;

            if (objective.getOrder() != expectedOrder) {
                throw new IllegalArgumentException(
                        "Quest objective order must start at 1 and be consecutive. Expected order "
                                + expectedOrder
                                + " but found "
                                + objective.getOrder()
                                + ".");
            }

            if (!objectiveIds.add(objective.getId())) {
                throw new IllegalArgumentException(
                        "Quest objective ID is duplicated in one step: " + objective.getId());
            }

            String objectiveTargetKey =
                    objective.getObjectiveType() + ":" + objective.getTargetId().orElse("");

            if (!objectiveTargets.add(objectiveTargetKey)) {
                throw new IllegalArgumentException(
                        "Quest step cannot repeat objective type and target: "
                                + objectiveTargetKey);
            }

            if (objective.getObjectiveType() == QuestObjectiveType.TALK_TO_NPC) {
                talkObjectiveCount++;
            }

            if (objective.getObjectiveType() == QuestObjectiveType.DELIVER_ITEM) {
                hasDeliveryObjective = true;
            }
        }
        if (talkObjectiveCount > 1) {
            throw new IllegalArgumentException(
                    "Quest step cannot contain more than one TALK_TO_NPC objective.");
        }

        if (hasDeliveryObjective && talkObjectiveCount != 1) {
            throw new IllegalArgumentException(
                    "Quest step containing DELIVER_ITEM must contain exactly one TALK_TO_NPC objective.");
        }
    }

    private static String validateTitle(String value) {
        String validatedTitle = validateText(value, "Quest step title");

        if (validatedTitle.contains("{") || validatedTitle.contains("}")) {
            throw new IllegalArgumentException("Quest step title cannot contain placeholders.");
        }

        return validatedTitle;
    }

    private static String validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }

        return value.trim();
    }

    public int getStep() {
        return step;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<QuestObjectiveDefinition> getObjectives() {
        return objectives;
    }
}

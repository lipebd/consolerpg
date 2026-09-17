package rpg.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Applies confirmed gameplay events to active quest progress.
public final class QuestProgressService {

    private final QuestProgress questProgress;

    public QuestProgressService(QuestProgress questProgress) {
        if (questProgress == null) {
            throw new IllegalArgumentException("Quest progress cannot be null.");
        }

        this.questProgress = questProgress;
    }

    public QuestProgressResult processEvents(List<QuestProgressEvent> events) {
        List<QuestProgressEvent> validatedEvents = validateEvents(events);
        List<QuestProgressUpdate> updates = new ArrayList<>();

        for (QuestProgressEvent event : validatedEvents) {
            processEvent(event, updates);
        }

        return new QuestProgressResult(updates);
    }

    private List<QuestProgressEvent> validateEvents(List<QuestProgressEvent> events) {
        if (events == null) {
            throw new IllegalArgumentException("Quest progress events cannot be null.");
        }

        for (QuestProgressEvent event : events) {
            if (event == null) {
                throw new IllegalArgumentException(
                        "Quest progress events cannot contain null entries.");
            }
        }

        return List.copyOf(events);
    }

    private void processEvent(QuestProgressEvent event, List<QuestProgressUpdate> updates) {
        List<QuestDefinition> activeQuests = questProgress.getActiveQuests();

        for (QuestDefinition questDefinition : activeQuests) {
            QuestStepDefinition currentStep =
                    questProgress
                            .getCurrentStep(questDefinition.getId())
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Active quest is missing its current step: "
                                                            + questDefinition.getId()));

            if (event.getEventType() == QuestProgressEventType.NPC_TALKED
                    && containsDeliveryObjective(currentStep)) {
                continue;
            }

            Optional<QuestObjectiveDefinition> matchingObjective =
                    findMatchingObjective(currentStep, event);

            if (matchingObjective.isEmpty()) {
                continue;
            }

            QuestObjectiveDefinition objective = matchingObjective.orElseThrow();

            int previousProgress =
                    questProgress.getObjectiveProgress(questDefinition.getId(), objective.getId());

            if (previousProgress >= objective.getRequiredQuantity()) {
                continue;
            }

            int resultingProgress =
                    questProgress.increaseObjectiveProgress(
                            questDefinition.getId(), objective.getId(), event.getQuantity());

            if (resultingProgress >= objective.getRequiredQuantity()) {
                updates.add(
                        QuestProgressUpdate.objectiveCompleted(
                                questDefinition.getId(),
                                currentStep.getStep(),
                                objective.getId(),
                                objective.getRequiredQuantity()));
            } else {
                updates.add(
                        QuestProgressUpdate.objectiveProgress(
                                questDefinition.getId(),
                                currentStep.getStep(),
                                objective.getId(),
                                resultingProgress,
                                objective.getRequiredQuantity()));
            }

            if (!questProgress.isCurrentStepComplete(questDefinition.getId())) {
                continue;
            }

            updates.add(
                    QuestProgressUpdate.stepCompleted(
                            questDefinition.getId(), currentStep.getStep()));

            boolean questCompleted = questProgress.advanceCompletedStep(questDefinition.getId());

            if (questCompleted) {
                updates.add(
                        QuestProgressUpdate.questCompleted(
                                questDefinition.getId(), currentStep.getStep()));
            }
        }
    }

    private Optional<QuestObjectiveDefinition> findMatchingObjective(
            QuestStepDefinition currentStep, QuestProgressEvent event) {
        QuestObjectiveType expectedObjectiveType = event.getEventType().getObjectiveType();

        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
            if (objective.getObjectiveType() == expectedObjectiveType
                    && objective.getTargetId().equals(event.getTargetId())) {
                return Optional.of(objective);
            }
        }

        return Optional.empty();
    }

    private boolean containsDeliveryObjective(QuestStepDefinition currentStep) {
        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
            if (objective.getObjectiveType() == QuestObjectiveType.DELIVER_ITEM) {
                return true;
            }
        }

        return false;
    }
}

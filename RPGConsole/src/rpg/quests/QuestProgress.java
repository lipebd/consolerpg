package rpg.quests;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Stores mutable quest progress for one campaign.
public final class QuestProgress {

	private final QuestCatalog questCatalog;
	private final Map<String, ActiveQuestState> activeQuestsById;
	private final Set<String> completedQuestIds;

	public QuestProgress(QuestCatalog questCatalog) {
		if (questCatalog == null) {
			throw new IllegalArgumentException(
					"Quest catalog cannot be null."
			);
		}

		this.questCatalog = questCatalog;
		this.activeQuestsById = new LinkedHashMap<>();
		this.completedQuestIds = new LinkedHashSet<>();
	}

	public QuestStatus getStatus(String questId) {
		QuestDefinition questDefinition =
				questCatalog.getById(questId);

		if (completedQuestIds.contains(questDefinition.getId())) {
			return QuestStatus.COMPLETED;
		}

		if (activeQuestsById.containsKey(questDefinition.getId())) {
			return QuestStatus.ACTIVE;
		}

		return QuestStatus.NOT_STARTED;
	}

	public List<QuestDefinition> getActiveQuests() {
		List<QuestDefinition> activeQuests = new ArrayList<>();

		for (QuestDefinition questDefinition : questCatalog.getAll()) {
			if (activeQuestsById.containsKey(
					questDefinition.getId())) {
				activeQuests.add(questDefinition);
			}
		}

		return List.copyOf(activeQuests);
	}

	public List<QuestDefinition> getCompletedQuests() {
		List<QuestDefinition> completedQuests = new ArrayList<>();

		for (QuestDefinition questDefinition : questCatalog.getAll()) {
			if (completedQuestIds.contains(
					questDefinition.getId())) {
				completedQuests.add(questDefinition);
			}
		}

		return List.copyOf(completedQuests);
	}

	public Optional<QuestStepDefinition> getCurrentStep(
			String questId) {
		QuestDefinition questDefinition =
				questCatalog.getById(questId);
		ActiveQuestState activeState =
				activeQuestsById.get(questDefinition.getId());

		if (activeState == null) {
			return Optional.empty();
		}

		return Optional.of(
				getStepDefinition(
						questDefinition,
						activeState.currentStep
				)
		);
	}

	public int getObjectiveProgress(
			String questId,
			String objectiveId) {
		QuestDefinition questDefinition =
				questCatalog.getById(questId);
		ActiveQuestState activeState =
				getActiveState(questDefinition);
		QuestObjectiveDefinition objectiveDefinition =
				getCurrentObjective(
						questDefinition,
						activeState,
						objectiveId
				);

		Integer currentProgress =
				activeState.objectiveProgressById.get(
						objectiveDefinition.getId()
				);

		if (currentProgress == null) {
			throw new IllegalStateException(
					"Active quest state is missing objective: "
							+ objectiveDefinition.getId()
			);
		}

		return currentProgress;
	}

	void activateQuest(String questId) {
		QuestDefinition questDefinition =
				questCatalog.getById(questId);
		QuestStatus currentStatus =
				getStatus(questDefinition.getId());

		if (currentStatus != QuestStatus.NOT_STARTED) {
			throw new IllegalStateException(
					"Quest cannot be activated from status "
							+ currentStatus
							+ ": "
							+ questDefinition.getId()
			);
		}

		QuestStepDefinition firstStep =
				getStepDefinition(questDefinition, 1);
		ActiveQuestState activeState =
				createActiveState(firstStep);

		activeQuestsById.put(
				questDefinition.getId(),
				activeState
		);
	}

	int increaseObjectiveProgress(
			String questId,
			String objectiveId,
			int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException(
					"Quest objective progress quantity must be positive."
			);
		}

		QuestDefinition questDefinition =
				questCatalog.getById(questId);
		ActiveQuestState activeState =
				getActiveState(questDefinition);
		QuestObjectiveDefinition objectiveDefinition =
				getCurrentObjective(
						questDefinition,
						activeState,
						objectiveId
				);

		int currentProgress =
				activeState.objectiveProgressById.get(
						objectiveDefinition.getId()
				);

		long increasedProgress =
				(long) currentProgress + quantity;

		int resultingProgress =
				(int) Math.min(
						increasedProgress,
						objectiveDefinition.getRequiredQuantity()
				);

		activeState.objectiveProgressById.put(
				objectiveDefinition.getId(),
				resultingProgress
		);

		return resultingProgress;
	}

	boolean isCurrentStepComplete(String questId) {
		QuestDefinition questDefinition =
				questCatalog.getById(questId);
		ActiveQuestState activeState =
				getActiveState(questDefinition);
		QuestStepDefinition currentStep =
				getStepDefinition(
						questDefinition,
						activeState.currentStep
				);

		for (QuestObjectiveDefinition objective
				: currentStep.getObjectives()) {
			int currentProgress =
					activeState.objectiveProgressById.get(
							objective.getId()
					);

			if (currentProgress
					< objective.getRequiredQuantity()) {
				return false;
			}
		}

		return true;
	}

	boolean isCurrentStepFinal(String questId) {
	    QuestDefinition questDefinition =
	            questCatalog.getById(questId);

	    ActiveQuestState activeState =
	            getActiveState(questDefinition);

	    return activeState.currentStep
	            == questDefinition.getSteps().size();
	}

	boolean advanceCompletedStep(String questId) {
		QuestDefinition questDefinition =
				questCatalog.getById(questId);
		ActiveQuestState activeState =
				getActiveState(questDefinition);

		if (!isCurrentStepComplete(questDefinition.getId())) {
			throw new IllegalStateException(
					"Quest step cannot advance before every objective "
							+ "is complete: "
							+ questDefinition.getId()
			);
		}

		int nextStep = activeState.currentStep + 1;

		if (nextStep > questDefinition.getSteps().size()) {
			completedQuestIds.add(questDefinition.getId());
			activeQuestsById.remove(questDefinition.getId());
			return true;
		}

		QuestStepDefinition nextStepDefinition =
				getStepDefinition(questDefinition, nextStep);
		ActiveQuestState nextActiveState =
				createActiveState(nextStepDefinition);

		activeQuestsById.put(
				questDefinition.getId(),
				nextActiveState
		);

		return false;
	}

	private ActiveQuestState getActiveState(
			QuestDefinition questDefinition) {
		ActiveQuestState activeState =
				activeQuestsById.get(questDefinition.getId());

		if (activeState == null) {
			throw new IllegalStateException(
					"Quest is not active: "
							+ questDefinition.getId()
			);
		}

		return activeState;
	}

	private QuestStepDefinition getStepDefinition(
			QuestDefinition questDefinition,
			int step) {
		if (step <= 0
				|| step > questDefinition.getSteps().size()) {
			throw new IllegalStateException(
					"Quest step is outside the definition range for "
							+ questDefinition.getId()
							+ ": "
							+ step
			);
		}

		return questDefinition.getSteps().get(step - 1);
	}

	private QuestObjectiveDefinition getCurrentObjective(
			QuestDefinition questDefinition,
			ActiveQuestState activeState,
			String objectiveId) {
		if (objectiveId == null || objectiveId.isBlank()) {
			throw new IllegalArgumentException(
					"Quest objective ID cannot be empty."
			);
		}

		QuestStepDefinition currentStep =
				getStepDefinition(
						questDefinition,
						activeState.currentStep
				);

		for (QuestObjectiveDefinition objective
				: currentStep.getObjectives()) {
			if (objective.getId().equals(objectiveId)) {
				return objective;
			}
		}

		throw new IllegalArgumentException(
				"Quest objective does not belong to the current step: "
						+ objectiveId
		);
	}

	private ActiveQuestState createActiveState(
			QuestStepDefinition stepDefinition) {
		Map<String, Integer> objectiveProgressById =
				new LinkedHashMap<>();

		for (QuestObjectiveDefinition objective
				: stepDefinition.getObjectives()) {
			objectiveProgressById.put(objective.getId(), 0);
		}

		return new ActiveQuestState(
				stepDefinition.getStep(),
				objectiveProgressById
		);
	}

	private static final class ActiveQuestState {

		private final int currentStep;
		private final Map<String, Integer> objectiveProgressById;

		private ActiveQuestState(
				int currentStep,
				Map<String, Integer> objectiveProgressById) {
			this.currentStep = currentStep;
			this.objectiveProgressById = objectiveProgressById;
		}
	}
}
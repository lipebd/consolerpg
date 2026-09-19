package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Stores one active quest and its current step in a campaign snapshot.
public final class ActiveQuestSnapshot {

	private final String questId;
	private final int currentStep;
	private final List<QuestObjectiveProgressSnapshot> objectiveProgressSnapshots;

	public ActiveQuestSnapshot(
			String questId,
			int currentStep,
			List<QuestObjectiveProgressSnapshot> objectiveProgressSnapshots
	) {
		if (questId == null || questId.isBlank()) {
			throw new IllegalArgumentException(
					"Active quest ID cannot be null or blank."
			);
		}

		if (currentStep <= 0) {
			throw new IllegalArgumentException(
					"Active quest current step must be positive."
			);
		}

		Objects.requireNonNull(
				objectiveProgressSnapshots,
				"Quest objective progress snapshots cannot be null."
		);

		if (objectiveProgressSnapshots.isEmpty()) {
			throw new IllegalArgumentException(
					"Active quest snapshot must contain objective progress."
			);
		}

		validateObjectiveProgressSnapshots(
				objectiveProgressSnapshots
		);

		this.questId = questId;
		this.currentStep = currentStep;
		this.objectiveProgressSnapshots =
				List.copyOf(objectiveProgressSnapshots);
	}

	private static void validateObjectiveProgressSnapshots(
			List<QuestObjectiveProgressSnapshot> objectiveProgressSnapshots
	) {
		Set<String> uniqueObjectiveIds = new HashSet<>();

		for (QuestObjectiveProgressSnapshot snapshot
				: objectiveProgressSnapshots) {
			Objects.requireNonNull(
					snapshot,
					"Quest objective progress snapshot cannot be null."
			);

			if (!uniqueObjectiveIds.add(snapshot.getObjectiveId())) {
				throw new IllegalArgumentException(
						"Quest objective ID is duplicated: "
								+ snapshot.getObjectiveId()
				);
			}
		}
	}

	public String getQuestId() {
		return questId;
	}

	public int getCurrentStep() {
		return currentStep;
	}

	public List<QuestObjectiveProgressSnapshot>
			getObjectiveProgressSnapshots() {
		return objectiveProgressSnapshots;
	}
}
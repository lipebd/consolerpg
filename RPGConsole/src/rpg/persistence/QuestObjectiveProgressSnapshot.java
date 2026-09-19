package rpg.persistence;

// Stores one current quest-objective counter in a campaign snapshot.
public final class QuestObjectiveProgressSnapshot {

	private final String objectiveId;
	private final int currentQuantity;

	public QuestObjectiveProgressSnapshot(
			String objectiveId,
			int currentQuantity
	) {
		if (objectiveId == null || objectiveId.isBlank()) {
			throw new IllegalArgumentException(
					"Quest objective ID cannot be null or blank."
			);
		}

		if (currentQuantity < 0) {
			throw new IllegalArgumentException(
					"Quest objective current quantity cannot be negative."
			);
		}

		this.objectiveId = objectiveId;
		this.currentQuantity = currentQuantity;
	}

	public String getObjectiveId() {
		return objectiveId;
	}

	public int getCurrentQuantity() {
		return currentQuantity;
	}
}
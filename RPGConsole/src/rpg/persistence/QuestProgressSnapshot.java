package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Stores active and completed quest state in a campaign snapshot.
public final class QuestProgressSnapshot {

	private final List<ActiveQuestSnapshot> activeQuests;
	private final List<String> completedQuestIds;

	public QuestProgressSnapshot(
			List<ActiveQuestSnapshot> activeQuests,
			List<String> completedQuestIds
	) {
		Objects.requireNonNull(
				activeQuests,
				"Active quest snapshots cannot be null."
		);

		Objects.requireNonNull(
				completedQuestIds,
				"Completed quest IDs cannot be null."
		);

		validateQuestIds(activeQuests, completedQuestIds);

		this.activeQuests = List.copyOf(activeQuests);
		this.completedQuestIds = List.copyOf(completedQuestIds);
	}

	private static void validateQuestIds(
			List<ActiveQuestSnapshot> activeQuests,
			List<String> completedQuestIds
	) {
		Set<String> activeQuestIds = new HashSet<>();

		for (ActiveQuestSnapshot activeQuest : activeQuests) {
			Objects.requireNonNull(
					activeQuest,
					"Active quest snapshot cannot be null."
			);

			if (!activeQuestIds.add(activeQuest.getQuestId())) {
				throw new IllegalArgumentException(
						"Active quest ID is duplicated: "
								+ activeQuest.getQuestId()
				);
			}
		}

		Set<String> completedIds = new HashSet<>();

		for (String completedQuestId : completedQuestIds) {
			if (completedQuestId == null
					|| completedQuestId.isBlank()) {
				throw new IllegalArgumentException(
						"Completed quest ID cannot be null or blank."
				);
			}

			if (!completedIds.add(completedQuestId)) {
				throw new IllegalArgumentException(
						"Completed quest ID is duplicated: "
								+ completedQuestId
				);
			}

			if (activeQuestIds.contains(completedQuestId)) {
				throw new IllegalArgumentException(
						"Quest cannot be both active and completed: "
								+ completedQuestId
				);
			}
		}
	}

	public List<ActiveQuestSnapshot> getActiveQuests() {
		return activeQuests;
	}

	public List<String> getCompletedQuestIds() {
		return completedQuestIds;
	}
}
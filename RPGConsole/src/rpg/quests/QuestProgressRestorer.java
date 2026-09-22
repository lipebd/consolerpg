package rpg.quests;

import java.util.Map;

// Reconstructs validated quest progress without replaying gameplay events.
public final class QuestProgressRestorer {

	private final QuestProgress questProgress;
	private boolean finished;

	public QuestProgressRestorer(QuestCatalog questCatalog) {
		this.questProgress = new QuestProgress(questCatalog);
		this.finished = false;
	}

	public void restoreActiveQuest(
			String questId,
			int currentStep,
			Map<String, Integer> objectiveProgressById
	) {
		requireOpen();

		questProgress.restoreActiveQuest(
				questId,
				currentStep,
				objectiveProgressById
		);
	}

	public void restoreCompletedQuest(String questId) {
		requireOpen();

		questProgress.restoreCompletedQuest(questId);
	}

	public QuestProgress finish() {
		requireOpen();

		finished = true;
		return questProgress;
	}

	private void requireOpen() {
		if (finished) {
			throw new IllegalStateException(
					"Quest progress restoration is already finished."
			);
		}
	}
}
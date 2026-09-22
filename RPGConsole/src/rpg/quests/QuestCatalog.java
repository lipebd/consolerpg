package rpg.quests;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to external quest definitions.
public final class QuestCatalog {

	private final Map<String, QuestDefinition> questsById;

	public QuestCatalog() {
		questsById = new LinkedHashMap<>();
	}

	public void register(QuestDefinition questDefinition) {
		if (questDefinition == null) {
			throw new IllegalArgumentException(
					"Quest definition cannot be null."
			);
		}

		String questId = questDefinition.getId();

		if (questsById.containsKey(questId)) {
			throw new IllegalArgumentException(
					"Quest ID is already registered: " + questId
			);
		}

		questsById.put(questId, questDefinition);
	}

	public QuestDefinition getById(String questId) {
		if (questId == null || questId.isBlank()) {
			throw new IllegalArgumentException(
					"Quest ID cannot be empty."
			);
		}

		QuestDefinition questDefinition = questsById.get(questId);

		if (questDefinition == null) {
			throw new IllegalArgumentException(
					"Quest not found: " + questId
			);
		}

		return questDefinition;
	}

	public List<QuestDefinition> getAll() {
		return List.copyOf(questsById.values());
	}
}
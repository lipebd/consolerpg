package rpg.world.scenarios;

import java.util.LinkedHashMap;
import java.util.Map;

// Stores dungeon definitions by scenario ID.
public final class DungeonDefinitionCatalog {

	private final Map<String, DungeonDefinition> definitionsByScenarioId;

	public DungeonDefinitionCatalog() {
		definitionsByScenarioId = new LinkedHashMap<>();
	}

	public void register(DungeonDefinition definition) {
		String scenarioId = definition.getScenarioId();

		if (definitionsByScenarioId.containsKey(scenarioId)) {
			throw new IllegalArgumentException("Dungeon definition is already registered: " + scenarioId);
		}

		definitionsByScenarioId.put(scenarioId, definition);
	}

	public DungeonDefinition getByScenarioId(String scenarioId) {
		DungeonDefinition definition = definitionsByScenarioId.get(scenarioId);

		if (definition == null) {
			throw new IllegalArgumentException("Dungeon definition not found: " + scenarioId);
		}

		return definition;
	}

	public boolean containsScenarioId(String scenarioId) {
		return definitionsByScenarioId.containsKey(scenarioId);
	}
}
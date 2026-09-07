package rpg.world.scenarios;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to scenario definitions.
public final class ScenarioDefinitionCatalog {

	private final Map<String, ScenarioDefinition> definitionsById;

	public ScenarioDefinitionCatalog() {
		definitionsById = new LinkedHashMap<>();
	}

	public void register(ScenarioDefinition definition) {
		String scenarioId = definition.getId();

		if (definitionsById.containsKey(scenarioId)) {
			throw new IllegalArgumentException("Scenario definition ID is already registered: " + scenarioId);
		}

		definitionsById.put(scenarioId, definition);
	}

	public ScenarioDefinition getById(String id) {
		ScenarioDefinition definition = definitionsById.get(id);

		if (definition == null) {
			throw new IllegalArgumentException("Scenario definition not found: " + id);
		}

		return definition;
	}

	public List<ScenarioDefinition> getAll() {
		return List.copyOf(definitionsById.values());
	}
}
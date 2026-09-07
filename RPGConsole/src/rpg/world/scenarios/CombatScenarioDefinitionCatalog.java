package rpg.world.scenarios;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores combat scenario definitions by scenario ID.
public final class CombatScenarioDefinitionCatalog {

	private final Map<String, CombatScenarioDefinition> definitionsByScenarioId;

	public CombatScenarioDefinitionCatalog() {
		definitionsByScenarioId = new LinkedHashMap<>();
	}

	public void register(CombatScenarioDefinition definition) {
		String scenarioId = definition.getScenarioId();

		if (definitionsByScenarioId.containsKey(scenarioId)) {
			throw new IllegalArgumentException("Combat scenario definition is already registered: " + scenarioId);
		}

		definitionsByScenarioId.put(scenarioId, definition);
	}

	public CombatScenarioDefinition getByScenarioId(String scenarioId) {
		CombatScenarioDefinition definition = definitionsByScenarioId.get(scenarioId);

		if (definition == null) {
			throw new IllegalArgumentException("Combat scenario definition not found: " + scenarioId);
		}

		return definition;
	}

	public boolean containsScenarioId(String scenarioId) {
		return definitionsByScenarioId.containsKey(scenarioId);
	}
	
	public List<CombatScenarioDefinition> getAll() {
	    return List.copyOf(
	            definitionsByScenarioId.values()
	    );
	}
}
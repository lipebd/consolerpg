package rpg.world.crafting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to crafter definitions.
public final class CrafterCatalog {

	private final Map<String, CrafterDefinition> craftersById;
	private final Map<String, List<CrafterDefinition>> craftersByScenarioId;

	public CrafterCatalog() {
		craftersById = new LinkedHashMap<>();
		craftersByScenarioId = new LinkedHashMap<>();
	}

	public void register(CrafterDefinition crafterDefinition) {
		if (crafterDefinition == null) {
			throw new IllegalArgumentException(
					"Crafter definition cannot be null."
			);
		}

		String crafterId = crafterDefinition.getId();

		if (craftersById.containsKey(crafterId)) {
			throw new IllegalArgumentException(
					"Crafter ID is already registered: " + crafterId
			);
		}

		craftersById.put(crafterId, crafterDefinition);
		craftersByScenarioId
				.computeIfAbsent(
						crafterDefinition.getScenarioId(),
						ignored -> new ArrayList<>()
				)
				.add(crafterDefinition);
	}

	public CrafterDefinition getById(String crafterId) {
		if (crafterId == null || crafterId.isBlank()) {
			throw new IllegalArgumentException("Crafter ID cannot be empty.");
		}

		CrafterDefinition crafterDefinition = craftersById.get(crafterId);

		if (crafterDefinition == null) {
			throw new IllegalArgumentException(
					"Crafter not found: " + crafterId
			);
		}

		return crafterDefinition;
	}

	public List<CrafterDefinition> getByScenarioId(String scenarioId) {
		List<CrafterDefinition> crafterDefinitions =
				craftersByScenarioId.get(scenarioId);

		if (crafterDefinitions == null) {
			return List.of();
		}

		return List.copyOf(crafterDefinitions);
	}

	public List<CrafterDefinition> getAll() {
		return List.copyOf(craftersById.values());
	}
}
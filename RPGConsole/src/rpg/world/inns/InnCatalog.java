package rpg.world.inns;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Stores and provides access to INN definitions.
public final class InnCatalog {

	private final Map<String, InnDefinition> innsById;
	private final Map<String, InnDefinition> innsByCityScenarioId;

	public InnCatalog() {
		innsById = new LinkedHashMap<>();
		innsByCityScenarioId = new LinkedHashMap<>();
	}

	public void register(InnDefinition innDefinition) {
		if (innDefinition == null) {
			throw new IllegalArgumentException("Inn definition cannot be null.");
		}

		String innId = innDefinition.getId();

		if (innsById.containsKey(innId)) {
			throw new IllegalArgumentException(
					"Inn ID is already registered: " + innId
			);
		}

		String cityScenarioId = innDefinition.getScenarioId();

		if (innsByCityScenarioId.containsKey(cityScenarioId)) {
			throw new IllegalArgumentException(
					"An Inn is already registered for city scenario: "
							+ cityScenarioId
			);
		}

		innsById.put(innId, innDefinition);
		innsByCityScenarioId.put(cityScenarioId, innDefinition);
	}

	public InnDefinition getById(String innId) {
		InnDefinition innDefinition = innsById.get(innId);

		if (innDefinition == null) {
			throw new IllegalArgumentException("Inn not found: " + innId);
		}

		return innDefinition;
	}

	public Optional<InnDefinition> findByScenarioId(String scenarioId) {
		return Optional.ofNullable(innsByCityScenarioId.get(scenarioId));
	}

	public List<InnDefinition> getAll() {
		return List.copyOf(innsById.values());
	}
}
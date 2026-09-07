package rpg.world.transport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to paid city transport definitions.
public final class CityTransportCatalog {

	private final Map<String, CityTransportDefinition> definitionsByDestinationScenarioId;

	public CityTransportCatalog() {
		definitionsByDestinationScenarioId = new LinkedHashMap<>();
	}

	public void register(CityTransportDefinition definition) {
		if (definition == null) {
			throw new IllegalArgumentException("City transport definition cannot be null.");
		}

		String destinationScenarioId = definition.getDestinationScenarioId();

		if (definitionsByDestinationScenarioId.containsKey(destinationScenarioId)) {
			throw new IllegalArgumentException(
					"City transport destination is already registered: " + destinationScenarioId
			);
		}

		definitionsByDestinationScenarioId.put(destinationScenarioId, definition);
	}

	public CityTransportDefinition getByDestinationScenarioId(String destinationScenarioId) {
		CityTransportDefinition definition = definitionsByDestinationScenarioId.get(destinationScenarioId);

		if (definition == null) {
			throw new IllegalArgumentException("City transport destination not found: " + destinationScenarioId);
		}

		return definition;
	}

	public List<CityTransportDefinition> getAll() {
		return List.copyOf(definitionsByDestinationScenarioId.values());
	}
}
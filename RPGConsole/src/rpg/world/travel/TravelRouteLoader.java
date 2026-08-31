package rpg.world.travel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.world.scenarios.ScenarioDefinitionCatalog;

// Loads travel route definitions from external data.
public final class TravelRouteLoader {

	private static final int EXPECTED_FIELD_COUNT = 6;

	public TravelCatalog load(Path filePath, ScenarioDefinitionCatalog scenarioDefinitionCatalog) throws IOException {
		TravelCatalog catalog = new TravelCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

			TravelRouteDefinition routeDefinition = parseDefinition(trimmedLine, scenarioDefinitionCatalog);

			catalog.register(routeDefinition);
		}

		return catalog;
	}

	private TravelRouteDefinition parseDefinition(String line, ScenarioDefinitionCatalog scenarioDefinitionCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException("Invalid travel route definition. Expected " + EXPECTED_FIELD_COUNT
					+ " fields but found " + fields.length + ": " + line);
		}

		String id = fields[0].trim();
		String sourceScenarioId = fields[1].trim();
		String destinationScenarioId = fields[2].trim();

		scenarioDefinitionCatalog.getById(sourceScenarioId);
		scenarioDefinitionCatalog.getById(destinationScenarioId);

		boolean initiallyUnlocked = parseBoolean(fields[3], "initially unlocked");

		String displayName = fields[4].trim();

		String description = fields[5].trim().replace("\\n", "\n");

		return new TravelRouteDefinition(id, sourceScenarioId, destinationScenarioId, initiallyUnlocked, displayName,
				description);
	}

	private boolean parseBoolean(String value, String fieldName) {
		String normalizedValue = value.trim();

		if (normalizedValue.equalsIgnoreCase("true")) {
			return true;
		}

		if (normalizedValue.equalsIgnoreCase("false")) {
			return false;
		}

		throw new IllegalArgumentException("Invalid " + fieldName + ": " + normalizedValue);
	}
}
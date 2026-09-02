package rpg.world.travel;

import rpg.world.scenarios.CombatScenarioDefinition;
import rpg.world.scenarios.CombatScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.world.scenarios.ScenarioDefinitionCatalog;

// Loads travel route definitions from external data.
public final class TravelRouteLoader {

	private static final int EXPECTED_FIELD_COUNT = 7;

	public TravelCatalog load(Path filePath, ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog) throws IOException {
		TravelCatalog catalog = new TravelCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

			TravelRouteDefinition routeDefinition = parseDefinition(trimmedLine, scenarioDefinitionCatalog,
					combatScenarioDefinitionCatalog);

			catalog.register(routeDefinition);
		}

		return catalog;
	}

	private TravelRouteDefinition parseDefinition(String line, ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException("Invalid travel route definition. Expected " + EXPECTED_FIELD_COUNT
					+ " fields but found " + fields.length + ": " + line);
		}

		String id = fields[0].trim();
		String sourceScenarioId = fields[1].trim();
		String destinationScenarioId = fields[2].trim();

		scenarioDefinitionCatalog.getById(sourceScenarioId);

		ScenarioDefinition destinationDefinition = scenarioDefinitionCatalog.getById(destinationScenarioId);

		boolean initiallyUnlocked = parseBoolean(fields[3], "initially unlocked");

		String displayName = fields[4].trim();

		String description = fields[5].trim().replace("\\n", "\n");

		String destinationPositionValue = fields[6].trim();

		if (destinationDefinition.getType() == ScenarioType.CITY) {
			if (!destinationPositionValue.isEmpty()) {
				throw new IllegalArgumentException("Travel destination exploration position "
						+ "must be empty for city: " + destinationScenarioId);
			}

			return new TravelRouteDefinition(id, sourceScenarioId, destinationScenarioId, initiallyUnlocked,
					displayName, description);
		}

		if (destinationPositionValue.isEmpty()) {
			throw new IllegalArgumentException("Travel destination exploration position "
					+ "is required for combat scenario: " + destinationScenarioId);
		}

		int destinationExplorationPosition = parseInteger(destinationPositionValue,
				"travel destination exploration position");

		CombatScenarioDefinition combatDefinition = combatScenarioDefinitionCatalog
				.getByScenarioId(destinationScenarioId);

		int explorationLength = combatDefinition.getExplorationSettings().getLength();

		if (destinationExplorationPosition < 1 || destinationExplorationPosition > explorationLength) {
			throw new IllegalArgumentException(
					"Travel destination exploration position " + "must be between 1 and " + explorationLength + ": "
							+ destinationScenarioId + ", position " + destinationExplorationPosition);
		}

		return new TravelRouteDefinition(id, sourceScenarioId, destinationScenarioId, initiallyUnlocked, displayName,
				description, destinationExplorationPosition);
	}

	private int parseInteger(String value, String fieldName) {
        String normalizedValue = value.trim();

        try {
                return Integer.parseInt(normalizedValue);

        } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                                "Invalid "
                                                + fieldName
                                                + ": "
                                                + normalizedValue,
                                exception
                );
        }
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
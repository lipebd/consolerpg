package rpg.world.scenarios;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.world.exploration.ExplorationSettings;

// Loads combat scenario definitions from external data.
public final class CombatScenarioLoader {

	private static final int EXPECTED_FIELD_COUNT = 13;

	public CombatScenarioDefinitionCatalog load(Path filePath, ScenarioDefinitionCatalog scenarioDefinitionCatalog)
			throws IOException {

		CombatScenarioDefinitionCatalog catalog = new CombatScenarioDefinitionCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

			CombatScenarioDefinition definition = parseDefinition(trimmedLine, scenarioDefinitionCatalog);

			catalog.register(definition);
		}

		return catalog;
	}

	private CombatScenarioDefinition parseDefinition(String line, ScenarioDefinitionCatalog scenarioDefinitionCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException("Invalid combat scenario definition. Expected " + EXPECTED_FIELD_COUNT
					+ " fields but found " + fields.length + ": " + line);
		}

		String scenarioId = fields[0].trim();

		ScenarioDefinition scenarioDefinition = scenarioDefinitionCatalog.getById(scenarioId);

		if (scenarioDefinition.getType() == ScenarioType.CITY) {
			throw new IllegalArgumentException("City cannot have combat scenario data: " + scenarioId);
		}

		int baseExperienceReward = parseInteger(fields[1], "base experience reward");

		int baseGoldReward = parseInteger(fields[2], "base gold reward");

		int explorationLength = parseInteger(fields[3], "exploration length");

		int battleChance = parseInteger(fields[4], "battle chance");

		int rewardChance = parseInteger(fields[5], "reward chance");

		int merchantChance = parseInteger(fields[6], "merchant chance");

		int epicEncounterChance = parseInteger(fields[7], "epic encounter chance");

		if (scenarioDefinition.getType() != ScenarioType.DUNGEON && epicEncounterChance > 0) {
			throw new IllegalArgumentException("Only dungeons can have an epic encounter chance: " + scenarioId);
		}

		int specialAreaChance = parseInteger(fields[8], "special area chance");

		int minEnemies = parseInteger(fields[9], "minimum enemy count");

		int maxEnemies = parseInteger(fields[10], "maximum enemy count");

		String normalAreaMessage = fields[11].trim().replace("\\n", "\n");

		String specialAreaMessage = fields[12].trim().replace("\\n", "\n");

		ExplorationSettings explorationSettings = new ExplorationSettings(explorationLength, battleChance, rewardChance,
				merchantChance, epicEncounterChance, specialAreaChance, minEnemies, maxEnemies, normalAreaMessage,
				specialAreaMessage);

		return new CombatScenarioDefinition(scenarioId, baseExperienceReward, baseGoldReward, explorationSettings);
	}

	private int parseInteger(String value, String fieldName) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Invalid " + fieldName + ": " + value.trim());
		}
	}
}
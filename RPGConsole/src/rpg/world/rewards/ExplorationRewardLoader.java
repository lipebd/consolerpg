package rpg.world.rewards;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.items.ItemCatalog;
import rpg.world.exploration.ExplorationAreaRestriction;
import rpg.world.exploration.ExplorationSettings;
import rpg.world.scenarios.CombatScenarioDefinition;
import rpg.world.scenarios.CombatScenarioDefinitionCatalog;

// Loads exploration reward entries from an external text file.
public final class ExplorationRewardLoader {

	private static final int EXPECTED_FIELD_COUNT = 7;

	public ExplorationRewardCatalog load(Path filePath, CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog,
			ItemCatalog itemCatalog) throws IOException {
		ExplorationRewardCatalog rewardCatalog = new ExplorationRewardCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

			registerEntry(trimmedLine, combatScenarioDefinitionCatalog, itemCatalog, rewardCatalog);
		}

		validateRequiredPools(combatScenarioDefinitionCatalog, rewardCatalog);

		return rewardCatalog;
	}

	private void registerEntry(String line, CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog,
			ItemCatalog itemCatalog, ExplorationRewardCatalog rewardCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException("Invalid exploration reward data. Expected " + EXPECTED_FIELD_COUNT
					+ " fields but found " + fields.length + ": " + line);
		}

		String scenarioId = fields[0].trim();

		ExplorationRewardType type = ExplorationRewardType.valueOf(fields[1].trim().toUpperCase());

		String rewardId = fields[2].trim();

		int minQuantity = parseInteger(fields[3], "minimum reward quantity");

		int maxQuantity = parseInteger(fields[4], "maximum reward quantity");

		int weight = parseInteger(fields[5], "reward weight");

		ExplorationAreaRestriction areaRestriction = ExplorationAreaRestriction.valueOf(fields[6].trim().toUpperCase());

		combatScenarioDefinitionCatalog.getByScenarioId(scenarioId);

		if (type == ExplorationRewardType.ITEM) {
			itemCatalog.getById(rewardId);
		}

		ExplorationRewardEntry rewardEntry = new ExplorationRewardEntry(type, rewardId, minQuantity, maxQuantity,
				weight, areaRestriction);

		rewardCatalog.register(scenarioId, rewardEntry);
	}

	private void validateRequiredPools(CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog,
			ExplorationRewardCatalog rewardCatalog) {
		for (CombatScenarioDefinition definition : combatScenarioDefinitionCatalog.getAll()) {
			ExplorationSettings settings = definition.getExplorationSettings();

			if (settings.getRewardChance() <= 0) {
				continue;
			}

			String scenarioId = definition.getScenarioId();

			boolean normalAreaPossible = settings.getSpecialAreaChance() < 100;

			boolean specialAreaPossible = settings.getSpecialAreaChance() > 0;

			if (normalAreaPossible && rewardCatalog.getAvailableEntries(scenarioId, false).isEmpty()) {
				throw new IllegalArgumentException(
						"Exploration reward pool is empty for normal areas of scenario: " + scenarioId);
			}

			if (specialAreaPossible && rewardCatalog.getAvailableEntries(scenarioId, true).isEmpty()) {
				throw new IllegalArgumentException(
						"Exploration reward pool is empty for special areas of scenario: " + scenarioId);
			}
		}
	}

	private int parseInteger(String value, String fieldName) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Invalid " + fieldName + ": " + value.trim());
		}
	}
}
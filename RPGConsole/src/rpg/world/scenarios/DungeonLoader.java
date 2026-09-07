package rpg.world.scenarios;

import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.characters.EnemyRank;
import rpg.enemies.EnemyDefinition;
import rpg.enemies.EnemyCatalog;

// Loads dungeon definitions from external data.
public final class DungeonLoader {

	private static final int EXPECTED_FIELD_COUNT = 3;

	public DungeonDefinitionCatalog load(Path filePath, ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog, EnemyCatalog enemyCatalog)
			throws IOException {

		DungeonDefinitionCatalog catalog = new DungeonDefinitionCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

			DungeonDefinition definition = parseDefinition(trimmedLine, scenarioDefinitionCatalog,
					combatScenarioDefinitionCatalog, enemyCatalog);

			catalog.register(definition);
		}

		return catalog;
	}

	private DungeonDefinition parseDefinition(String line, ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog, EnemyCatalog enemyCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException("Invalid dungeon definition. Expected " + EXPECTED_FIELD_COUNT
					+ " fields but found " + fields.length + ": " + line);
		}

		String scenarioId = fields[0].trim();

		ScenarioDefinition scenarioDefinition = scenarioDefinitionCatalog.getById(scenarioId);

		if (scenarioDefinition.getType() != ScenarioType.DUNGEON) {
			throw new IllegalArgumentException("Scenario is not a dungeon: " + scenarioId);
		}

		if (!combatScenarioDefinitionCatalog.containsScenarioId(scenarioId)) {
			throw new IllegalArgumentException("Dungeon is missing combat scenario data: " + scenarioId);
		}

		List<String> bossEnemyIds = parseEnemyIds(fields[1]);

		List<String> epicEnemyIds = parseEnemyIds(fields[2]);

		DungeonDefinition definition = new DungeonDefinition(scenarioId, bossEnemyIds, epicEnemyIds);

		validateEnemyRanks(definition.getBossEnemyIds(), EnemyRank.BOSS, "boss", enemyCatalog);

		validateEnemyRanks(definition.getEpicEnemyIds(), EnemyRank.EPIC, "epic", enemyCatalog);

		return definition;
	}

	private List<String> parseEnemyIds(String field) {
		return Arrays.stream(field.split(",", -1)).map(String::trim).toList();
	}

	private void validateEnemyRanks(List<String> enemyIds, EnemyRank expectedRank, String groupName,
			EnemyCatalog enemyCatalog) {
		for (String enemyId : enemyIds) {
			EnemyDefinition enemyDefinition = enemyCatalog.getById(enemyId);

			if (enemyDefinition.getRank() != expectedRank) {
				throw new IllegalArgumentException(
						"Dungeon " + groupName + " enemy must have " + expectedRank + " rank: " + enemyId);
			}
		}
	}
}
package rpg.world.scenarios;


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

		String bossEnemyId = fields[1].trim();
		String epicEnemyId = fields[2].trim();

		EnemyDefinition bossDefinition = enemyCatalog.getById(bossEnemyId);

		if (bossDefinition.getRank() != EnemyRank.BOSS) {
		    throw new IllegalArgumentException(
		            "Dungeon boss enemy must have BOSS rank: " + bossEnemyId);
		}

		EnemyDefinition epicDefinition = enemyCatalog.getById(epicEnemyId);

		if (epicDefinition.getRank() != EnemyRank.EPIC) {
		    throw new IllegalArgumentException(
		            "Dungeon epic enemy must have EPIC rank: " + epicEnemyId);
		}

		return new DungeonDefinition(scenarioId, bossEnemyId, epicEnemyId);
	}
}
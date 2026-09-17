package rpg.world.scenarios;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import rpg.characters.EnemyRank;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDefinition;
import rpg.world.exploration.ExplorationAreaRestriction;
import rpg.world.exploration.ExplorationSettings;

// Loads dungeon definitions from external data.
public final class DungeonLoader {

    private static final int EXPECTED_FIELD_COUNT = 8;

    public DungeonDefinitionCatalog load(
            Path filePath,
            ScenarioDefinitionCatalog scenarioDefinitionCatalog,
            CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog,
            EnemyCatalog enemyCatalog)
			throws IOException {
		DungeonDefinitionCatalog catalog = new DungeonDefinitionCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

            DungeonDefinition definition =
                    parseDefinition(
                            trimmedLine,
                            scenarioDefinitionCatalog,
                            combatScenarioDefinitionCatalog,
                            enemyCatalog);

			catalog.register(definition);
		}

		return catalog;
	}

    private DungeonDefinition parseDefinition(
            String line,
            ScenarioDefinitionCatalog scenarioDefinitionCatalog,
            CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog,
            EnemyCatalog enemyCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Invalid dungeon definition. Expected "
                            + EXPECTED_FIELD_COUNT
                            + " fields but found "
                            + fields.length
                            + ": "
                            + line);
		}

		String scenarioId = fields[0].trim();

		ScenarioDefinition scenarioDefinition = scenarioDefinitionCatalog.getById(scenarioId);

		if (scenarioDefinition.getType() != ScenarioType.DUNGEON) {
			throw new IllegalArgumentException("Scenario is not a dungeon: " + scenarioId);
		}

		if (!combatScenarioDefinitionCatalog.containsScenarioId(scenarioId)) {
            throw new IllegalArgumentException(
                    "Dungeon is missing combat scenario data: " + scenarioId);
		}

        CombatScenarioDefinition combatDefinition =
                combatScenarioDefinitionCatalog.getByScenarioId(scenarioId);
        ExplorationSettings explorationSettings = combatDefinition.getExplorationSettings();

        if (explorationSettings.getEpicEncounterChance() <= 0) {
            throw new IllegalArgumentException(
                    "Dungeon must define a positive epic encounter chance: " + scenarioId);
        }

        List<String> bossEnemyIds = parseEnemyIds(fields[1]);
        List<String> recurringBossEnemyIds = parseEnemyIds(fields[2]);
        int recurringBossCooldownHours = parseInteger(fields[3], "recurring boss cooldown hours");
        List<String> epicEnemyIds = parseEnemyIds(fields[4]);
        int epicMinPosition = parseInteger(fields[5], "epic minimum position");
        ExplorationAreaRestriction epicAreaRestriction = parseAreaRestriction(fields[6]);
        int epicEnemyCooldownHours = parseInteger(fields[7], "epic enemy cooldown hours");

        DungeonDefinition definition =
                new DungeonDefinition(
                        scenarioId,
                        bossEnemyIds,
                        recurringBossEnemyIds,
                        recurringBossCooldownHours,
                        epicEnemyIds,
                        epicMinPosition,
                        epicAreaRestriction,
                        epicEnemyCooldownHours);

        if (definition.getEpicMinPosition() >= explorationSettings.getLength()) {
            throw new IllegalArgumentException(
                    "Epic minimum position must be before the final position of dungeon "
                            + scenarioId
                            + ": "
                            + definition.getEpicMinPosition());
        }

        if (definition.getEpicAreaRestriction() == ExplorationAreaRestriction.SPECIAL_ONLY
                && (explorationSettings.getSpecialAreaChance() <= 0
                        || explorationSettings.getLength() < 3)) {
            throw new IllegalArgumentException(
                    "SPECIAL_ONLY epic encounter requires possible intermediate special areas: "
                            + scenarioId);
        }

        validateEnemyRanks(definition.getBossEnemyIds(), EnemyRank.BOSS, "boss", enemyCatalog);
        validateEnemyRanks(
                definition.getRecurringBossEnemyIds(),
                EnemyRank.BOSS,
                "recurring boss",
                enemyCatalog);
		validateEnemyRanks(definition.getEpicEnemyIds(), EnemyRank.EPIC, "epic", enemyCatalog);

		return definition;
	}

	private List<String> parseEnemyIds(String field) {
		return Arrays.stream(field.split(",", -1)).map(String::trim).toList();
	}

    private ExplorationAreaRestriction parseAreaRestriction(String field) {
        String value = field.trim();

        try {
            return ExplorationAreaRestriction.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid epic area restriction: " + value);
        }
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value.trim());
        }
    }

    private void validateEnemyRanks(
            List<String> enemyIds,
            EnemyRank expectedRank,
            String groupName,
			EnemyCatalog enemyCatalog) {
		for (String enemyId : enemyIds) {
			EnemyDefinition enemyDefinition = enemyCatalog.getById(enemyId);

			if (enemyDefinition.getRank() != expectedRank) {
				throw new IllegalArgumentException(
                        "Dungeon "
                                + groupName
                                + " enemy must have "
                                + expectedRank
                                + " rank: "
                                + enemyId);
			}
		}
	}
}
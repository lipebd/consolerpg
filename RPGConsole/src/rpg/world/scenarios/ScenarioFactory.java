package rpg.world.scenarios;

import rpg.world.exploration.ExplorationSettings;

// Creates runtime scenario objects from loaded definitions.
public final class ScenarioFactory {

	public Scenario create(
			ScenarioDefinition scenarioDefinition,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog,
			DungeonDefinitionCatalog dungeonDefinitionCatalog
	) {
		return switch (scenarioDefinition.getType()) {
		case CITY -> createCity(scenarioDefinition);

		case FIELD -> createField(scenarioDefinition, combatScenarioDefinitionCatalog);

		case DUNGEON -> createDungeon(scenarioDefinition, combatScenarioDefinitionCatalog, dungeonDefinitionCatalog);
		};
	}

	private City createCity(ScenarioDefinition scenarioDefinition) {
		return new City(scenarioDefinition.getId(), scenarioDefinition.getName(), scenarioDefinition.getDescription());
	}

	private Field createField(
			ScenarioDefinition scenarioDefinition,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog
	) {
		CombatScenarioDefinition combatDefinition = combatScenarioDefinitionCatalog
				.getByScenarioId(scenarioDefinition.getId());

		return new Field(
				scenarioDefinition.getId(),
				scenarioDefinition.getName(),
				scenarioDefinition.getDescription(),
				combatDefinition.getBaseExperienceReward(),
				combatDefinition.getBaseGoldReward(),
				combatDefinition.getExplorationSettings(),
				combatDefinition.getPreferredRescueScenarioId(),
				combatDefinition.getFallbackRescueScenarioId(),
				combatDefinition.getRescueDescription()
		);
	}

	private Dungeon createDungeon(
			ScenarioDefinition scenarioDefinition,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog,
			DungeonDefinitionCatalog dungeonDefinitionCatalog
	) {
		CombatScenarioDefinition combatDefinition = combatScenarioDefinitionCatalog
				.getByScenarioId(scenarioDefinition.getId());

		DungeonDefinition dungeonDefinition = dungeonDefinitionCatalog.getByScenarioId(scenarioDefinition.getId());

		ExplorationSettings explorationSettings = combatDefinition.getExplorationSettings();

		DungeonSettings dungeonSettings = new DungeonSettings(
				dungeonDefinition.getBossEnemyIds(),
				dungeonDefinition.getNarrativeBossLevel(),
				dungeonDefinition.getRecurringBossEnemyIds(),
				dungeonDefinition.getRecurringBossMinLevel(),
				dungeonDefinition.getRecurringBossMaxLevel(),
				dungeonDefinition.getRecurringBossCooldownHours(),
				dungeonDefinition.getEpicEnemyIds(),
				dungeonDefinition.getEpicMinLevel(),
				dungeonDefinition.getEpicMaxLevel(),
				dungeonDefinition.getEpicMinPosition(),
				dungeonDefinition.getEpicAreaRestriction(),
				dungeonDefinition.getEpicEnemyCooldownHours(),
				explorationSettings.getLength()
		);

		return new Dungeon(
				scenarioDefinition.getId(),
				scenarioDefinition.getName(),
				scenarioDefinition.getDescription(),
				combatDefinition.getBaseExperienceReward(),
				combatDefinition.getBaseGoldReward(),
				explorationSettings,
				combatDefinition.getPreferredRescueScenarioId(),
				combatDefinition.getFallbackRescueScenarioId(),
				combatDefinition.getRescueDescription(),
				dungeonSettings
		);
	}
}

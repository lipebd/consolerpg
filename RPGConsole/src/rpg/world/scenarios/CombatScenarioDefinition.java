package rpg.world.scenarios;

import rpg.world.exploration.ExplorationSettings;

// Stores reusable data specific to combat scenarios.
public final class CombatScenarioDefinition {

	private final String scenarioId;
	private final int baseExperienceReward;
	private final int baseGoldReward;
	private final ExplorationSettings explorationSettings;

	public CombatScenarioDefinition(String scenarioId, int baseExperienceReward, int baseGoldReward,
			ExplorationSettings explorationSettings) {
		if (scenarioId == null || scenarioId.isBlank()) {
			throw new IllegalArgumentException("Scenario ID cannot be empty.");
		}

		if (baseExperienceReward < 0) {
			throw new IllegalArgumentException("Base experience reward cannot be negative.");
		}

		if (baseGoldReward < 0) {
			throw new IllegalArgumentException("Base gold reward cannot be negative.");
		}

		if (explorationSettings == null) {
			throw new IllegalArgumentException("Exploration settings cannot be null.");
		}

		this.scenarioId = scenarioId;
		this.baseExperienceReward = baseExperienceReward;
		this.baseGoldReward = baseGoldReward;
		this.explorationSettings = explorationSettings;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public int getBaseExperienceReward() {
		return baseExperienceReward;
	}

	public int getBaseGoldReward() {
		return baseGoldReward;
	}

	public ExplorationSettings getExplorationSettings() {
		return explorationSettings;
	}
}
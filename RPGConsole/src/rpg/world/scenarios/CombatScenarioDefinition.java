package rpg.world.scenarios;

import java.util.Optional;

import rpg.world.exploration.ExplorationSettings;

// Stores reusable data specific to combat scenarios.
public final class CombatScenarioDefinition {

	private final String scenarioId;
	private final int baseExperienceReward;
	private final int baseGoldReward;
	private final ExplorationSettings explorationSettings;
	private final Optional<String> preferredRescueScenarioId;
	private final String fallbackRescueScenarioId;
	private final String rescueDescription;

	public CombatScenarioDefinition(
			String scenarioId,
			int baseExperienceReward,
			int baseGoldReward,
			ExplorationSettings explorationSettings,
			Optional<String> preferredRescueScenarioId,
			String fallbackRescueScenarioId,
			String rescueDescription
	) {
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

		if (preferredRescueScenarioId == null) {
			throw new IllegalArgumentException("Preferred rescue scenario ID optional cannot be null.");
		}

		if (preferredRescueScenarioId.isPresent() && preferredRescueScenarioId.get().isBlank()) {
			throw new IllegalArgumentException("Preferred rescue scenario ID cannot be empty when present.");
		}

		if (fallbackRescueScenarioId == null || fallbackRescueScenarioId.isBlank()) {
			throw new IllegalArgumentException("Fallback rescue scenario ID cannot be empty.");
		}

		if (rescueDescription == null || rescueDescription.isBlank()) {
			throw new IllegalArgumentException("Rescue description cannot be empty.");
		}

		if (preferredRescueScenarioId.isPresent() && preferredRescueScenarioId.get().equals(fallbackRescueScenarioId)) {
			throw new IllegalArgumentException("Preferred and fallback rescue scenario IDs must be different.");
		}

		this.scenarioId = scenarioId;
		this.baseExperienceReward = baseExperienceReward;
		this.baseGoldReward = baseGoldReward;
		this.explorationSettings = explorationSettings;
		this.preferredRescueScenarioId = preferredRescueScenarioId;
		this.fallbackRescueScenarioId = fallbackRescueScenarioId;
		this.rescueDescription = rescueDescription;
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

	public Optional<String> getPreferredRescueScenarioId() {
		return preferredRescueScenarioId;
	}

	public String getFallbackRescueScenarioId() {
		return fallbackRescueScenarioId;
	}

	public String getRescueDescription() {
		return rescueDescription;
	}
}

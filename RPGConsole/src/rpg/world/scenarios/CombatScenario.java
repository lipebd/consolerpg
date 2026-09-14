package rpg.world.scenarios;

import java.util.Objects;
import java.util.Optional;

import rpg.world.exploration.ExplorationSettings;

// Represents a scenario where combat encounters can occur.
public abstract class CombatScenario extends Scenario {

	private final ExplorationSettings explorationSettings;
	private final int baseExperienceReward;
	private final int baseGoldReward;
	private final Optional<String> preferredRescueScenarioId;
	private final String fallbackRescueScenarioId;
	private final String rescueDescription;

	protected CombatScenario(
			String id,
			String name,
			String description,
			int baseExperienceReward,
			int baseGoldReward,
			ExplorationSettings explorationSettings,
			Optional<String> preferredRescueScenarioId,
			String fallbackRescueScenarioId,
			String rescueDescription
	) {
		super(id, name, description);

		this.baseExperienceReward = baseExperienceReward;
		this.baseGoldReward = baseGoldReward;

		this.explorationSettings = Objects.requireNonNull(explorationSettings, "Exploration settings cannot be null.");
		this.preferredRescueScenarioId = Objects
				.requireNonNull(preferredRescueScenarioId, "Preferred rescue scenario ID optional cannot be null.");

		this.fallbackRescueScenarioId = Objects
				.requireNonNull(fallbackRescueScenarioId, "Fallback rescue scenario ID cannot be null.");

		this.rescueDescription = Objects.requireNonNull(rescueDescription, "Rescue description cannot be null.");
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

package rpg.world.scenarios;

import java.util.Optional;

import rpg.world.exploration.ExplorationSettings;


// Represents an open field that can contain exploration and encounters.
public class Field extends CombatScenario {

	public Field(
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
		super(
		        id,
		        name,
		        description,
		        baseExperienceReward,
		        baseGoldReward,
		        explorationSettings,
		        preferredRescueScenarioId,
		        fallbackRescueScenarioId,
		        rescueDescription
		);
	}

}

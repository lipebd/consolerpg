package rpg.world.scenarios;

import rpg.world.exploration.ExplorationSettings;


// Represents an open field that can contain exploration and encounters.
public class Field extends CombatScenario {

	public Field(
	        String id,
	        String name,
	        String description,
	        int baseExperienceReward,
	        int baseGoldReward,
	        ExplorationSettings explorationSettings
	) {
	    super(
	            id,
	            name,
	            description,
	            baseExperienceReward,
	            baseGoldReward,
	            explorationSettings
	    );
	}

}
package rpg.world.scenarios;

import java.util.Optional;

import rpg.world.exploration.ExplorationSettings;

// Represents an explorable dungeon with combat encounters.
public class Dungeon extends CombatScenario {
	
	private final DungeonSettings dungeonSettings;

	public Dungeon(
	        String id,
	        String name,
	        String description,
	        int baseExperienceReward,
	        int baseGoldReward,
	        ExplorationSettings explorationSettings,
	        Optional<String> preferredRescueScenarioId,
	        String fallbackRescueScenarioId,
	        String rescueDescription,
	        DungeonSettings dungeonSettings
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

	    this.dungeonSettings = dungeonSettings;
	}
    
    public DungeonSettings getDungeonSettings() {
        return dungeonSettings;
    }
}

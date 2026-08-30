package rpg.world.scenarios;

import java.util.List;

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
            DungeonSettings dungeonSettings
    ) {
        super(
                id,
                name,
                description,
                baseExperienceReward,
                baseGoldReward,
                explorationSettings
        );
        this.dungeonSettings = dungeonSettings;
    }

    @Override
    public List<ScenarioAction> getAvailableActions() {
        return List.of(
                ScenarioAction.EXPLORE,
                ScenarioAction.INVENTORY,
                ScenarioAction.VIEW_STATUS,
                ScenarioAction.TRAVEL
        );
    }
    
    public DungeonSettings getDungeonSettings() {
        return dungeonSettings;
    }
}
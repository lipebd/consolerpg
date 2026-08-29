package rpg.world.scenarios;

import java.util.List;

// Represents an open field that can contain exploration and encounters.
public class Field extends CombatScenario {

	public Field(
	        String name,
	        String description,
	        int baseExperienceReward,
	        int baseGoldReward
	) {
	    super(
	            name,
	            description,
	            baseExperienceReward,
	            baseGoldReward
	    );
	}

    @Override
    public List<ScenarioAction> getAvailableActions() {
        return List.of(
                ScenarioAction.EXPLORE,
                ScenarioAction.VIEW_STATUS,
                ScenarioAction.TRAVEL
        );
    }
}
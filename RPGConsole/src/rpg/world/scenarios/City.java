package rpg.world.scenarios;

import java.util.List;

// Represents a city with services and travel options.
public class City extends Scenario {

    public City(
            String id,
            String name,
            String description
    ) {
        super(
                id,
                name,
                description
        );
    }

    @Override
    public List<ScenarioAction> getAvailableActions() {
        return List.of(
                ScenarioAction.INVENTORY,
                ScenarioAction.VIEW_STATUS,
                ScenarioAction.TRAVEL
        );
    }
}
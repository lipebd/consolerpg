package rpg.world.scenarios;

import java.util.List;

// Represents the common structure shared by all game scenarios.
public abstract class Scenario {

    protected final String name;
    protected final String description;

    protected Scenario(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract List<ScenarioAction> getAvailableActions();
}
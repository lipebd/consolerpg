package rpg.world.scenarios;

import java.util.List;

// Represents the common structure shared by all game scenarios.
public abstract class Scenario {

	private final String id;
    private final String name;
    private final String description;

    protected Scenario(String id, String name, String description) {
        this.id = id;
    	this.name = name;
        this.description = description;
    }
    
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract List<ScenarioAction> getAvailableActions();
}
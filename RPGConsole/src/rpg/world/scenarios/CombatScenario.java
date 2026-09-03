package rpg.world.scenarios;
import java.util.Objects;
import rpg.world.exploration.ExplorationSettings;

// Represents a scenario where combat encounters can occur.
public abstract class CombatScenario extends Scenario {

	private final ExplorationSettings explorationSettings;
    private final int baseExperienceReward;
    private final int baseGoldReward;

    protected CombatScenario(
            String id,
            String name,
            String description,
            int baseExperienceReward,
            int baseGoldReward,
            ExplorationSettings explorationSettings
    ) {
        super(id, name, description);

        this.baseExperienceReward = baseExperienceReward;
        this.baseGoldReward = baseGoldReward;

        this.explorationSettings = Objects.requireNonNull(
                explorationSettings,
                "Exploration settings cannot be null."
        );
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
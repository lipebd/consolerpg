package rpg.world.scenarios;

// Represents a scenario where combat encounters can occur.
public abstract class CombatScenario extends Scenario {

    private final int baseExperienceReward;
    private final int baseGoldReward;

    protected CombatScenario(
            String name,
            String description,
            int baseExperienceReward,
            int baseGoldReward
    ) {
        super(name, description);

        this.baseExperienceReward = baseExperienceReward;
        this.baseGoldReward = baseGoldReward;
    }

    public int getBaseExperienceReward() {
        return baseExperienceReward;
    }

    public int getBaseGoldReward() {
        return baseGoldReward;
    }
}
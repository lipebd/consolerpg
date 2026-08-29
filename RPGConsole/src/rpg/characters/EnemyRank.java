package rpg.characters;

// Defines an enemy rank and its reward multiplier.
public enum EnemyRank {

    COMMON("Comum", 1.0),
    VETERAN("Veterano", 1.2);

    private final String displayName;
    private final double rewardMultiplier;

    EnemyRank(
            String displayName,
            double rewardMultiplier
    ) {
        this.displayName = displayName;
        this.rewardMultiplier = rewardMultiplier;
    }

    public double getRewardMultiplier() {
        return rewardMultiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
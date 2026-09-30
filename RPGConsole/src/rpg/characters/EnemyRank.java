package rpg.characters;

// Defines an enemy rank and its reward multiplier.
public enum EnemyRank {

    COMMON("Comum", 1.0),
    VETERAN("Veterano", 1.2),
	LEADER("Líder", 1.5),
	BOSS("Boss", 2.5),
	EPIC("Épico", 10.0),
	LEGENDARY("Lendário", 25.0);
	

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
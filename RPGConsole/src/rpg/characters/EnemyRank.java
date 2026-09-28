package rpg.characters;

//Defines an enemy rank and its stat and reward multipliers.
public enum EnemyRank {

	COMMON("Comum", 1.0),
	VETERAN("Veterano", 1.5),
	LEADER("Líder", 1.75),
	BOSS("Boss", 2.5),
	EPIC("Épico", 5.0),
	LEGENDARY("Lendário", 25.0);

	private final String displayName;
	private final double rewardMultiplier;

	EnemyRank(String displayName, double rewardMultiplier) {
		this.displayName = displayName;
		this.rewardMultiplier = rewardMultiplier;
	}

	public double getRewardMultiplier() {
		return rewardMultiplier;
	}

	public int getHpMultiplierPercent() {
		return switch (this) {
		case COMMON -> 100;
		case VETERAN -> 130;
		case LEADER -> 200;
		case BOSS -> 300;
		case EPIC -> 400;
		case LEGENDARY ->
			throw new IllegalArgumentException("Stat multipliers are not configured for LEGENDARY enemies.");
		};
	}

	public int getOffensiveMultiplierPercent() {
		return switch (this) {
		case COMMON -> 100;
		case VETERAN -> 110;
		case LEADER -> 120;
		case BOSS -> 130;
		case EPIC -> 140;
		case LEGENDARY ->
			throw new IllegalArgumentException("Stat multipliers are not configured for LEGENDARY enemies.");
		};
	}

	public int getMaximumAdditionalSkillCount() {
		return switch (this) {
		case COMMON -> 2;
		case VETERAN -> 3;
		case LEADER -> 4;
		case BOSS -> 5;
		case EPIC -> 6;
		case LEGENDARY ->
			throw new IllegalArgumentException("Skill limit is not configured for LEGENDARY enemies.");
		};
	}

	@Override
	public String toString() {
		return displayName;
	}
}
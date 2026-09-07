package rpg.world.exploration;

// Stores the exploration configuration of a combat scenario.
public final class ExplorationSettings {

	private final int length;
	private final int battleChance;
	private final int rewardChance;
	private final int merchantChance;
	private final int epicEncounterChance;
	private final int specialAreaChance;
	private final int minEnemies;
	private final int maxEnemies;
	private final String normalAreaMessage;
	private final String specialAreaMessage;

	public ExplorationSettings(int length, int battleChance, int rewardChance, int merchantChance,
			int epicEncounterChance, int specialAreaChance, int minEnemies, int maxEnemies, String normalAreaMessage,
			String specialAreaMessage) {
		if (length < 1) {
			throw new IllegalArgumentException("Exploration length must be at least one.");
		}

		validateChance(battleChance, "Battle");
		validateChance(rewardChance, "Reward");
		validateChance(merchantChance, "Merchant");
		validateChance(specialAreaChance, "Special area");
		validateChance(epicEncounterChance, "Epic encounter");

		int totalEventChance = battleChance + rewardChance + merchantChance + epicEncounterChance;

		if (totalEventChance > 100) {
			throw new IllegalArgumentException("The total event chance cannot exceed 100.");
		}

		if (minEnemies < 1) {
			throw new IllegalArgumentException("Minimum enemy count must be at least one.");
		}

		if (maxEnemies < minEnemies) {
			throw new IllegalArgumentException("Maximum enemy count cannot be lower than the minimum.");
		}

		if (normalAreaMessage == null || normalAreaMessage.isBlank()) {
			throw new IllegalArgumentException("Normal area message cannot be empty.");
		}

		if (specialAreaMessage == null || specialAreaMessage.isBlank()) {
			throw new IllegalArgumentException("Special area message cannot be empty.");
		}

		this.length = length;
		this.battleChance = battleChance;
		this.rewardChance = rewardChance;
		this.merchantChance = merchantChance;
		this.epicEncounterChance = epicEncounterChance;
		this.specialAreaChance = specialAreaChance;
		this.minEnemies = minEnemies;
		this.maxEnemies = maxEnemies;
		this.normalAreaMessage = normalAreaMessage;
		this.specialAreaMessage = specialAreaMessage;
	}

	private void validateChance(int chance, String chanceName) {
		if (chance < 0 || chance > 100) {
			throw new IllegalArgumentException(chanceName + " chance must be between 0 and 100.");
		}
	}

	public int getLength() {
		return length;
	}

	public int getBattleChance() {
		return battleChance;
	}

	public int getRewardChance() {
		return rewardChance;
	}

	public int getMerchantChance() {
		return merchantChance;
	}

	public int getEpicEncounterChance() {
		return epicEncounterChance;
	}

	public int getSpecialAreaChance() {
		return specialAreaChance;
	}

	public int getMinEnemies() {
		return minEnemies;
	}

	public int getMaxEnemies() {
		return maxEnemies;
	}

	public String getNormalAreaMessage() {
		return normalAreaMessage;
	}

	public String getSpecialAreaMessage() {
		return specialAreaMessage;
	}
}
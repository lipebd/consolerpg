package rpg.battle.rewards;

// Stores temporary party-wide reward modifiers.
public final class PartyRewardModifierState {

	private int experienceModifierPercent;
	private int experienceVictoriesRemaining;

	private int goldModifierPercent;
	private int goldVictoriesRemaining;

	public boolean activateExperienceModifier(int modifierPercent, int rewardedVictories) {
		return activateModifiers(modifierPercent, 0, rewardedVictories);
	}

	public boolean activateGoldModifier(int modifierPercent, int rewardedVictories) {
		return activateModifiers(0, modifierPercent, rewardedVictories);
	}

	public boolean activateModifiers(int experiencePercent, int goldPercent, int rewardedVictories) {
		if (experiencePercent < 0 || goldPercent < 0 || (experiencePercent == 0 && goldPercent == 0)) {
			throw new IllegalArgumentException("At least one reward modifier must be positive.");
		}

		if (rewardedVictories < 1) {
			throw new IllegalArgumentException("Rewarded victories must be positive.");
		}

		if ((experiencePercent > 0 && hasActiveExperienceModifier()) || (goldPercent > 0 && hasActiveGoldModifier())) {
			return false;
		}

		if (experiencePercent > 0) {
			experienceModifierPercent = experiencePercent;
			experienceVictoriesRemaining = rewardedVictories;
		}

		if (goldPercent > 0) {
			goldModifierPercent = goldPercent;
			goldVictoriesRemaining = rewardedVictories;
		}

		return true;
	}

	public void consumeRewardedVictory() {
		if (hasActiveExperienceModifier()) {
			experienceVictoriesRemaining--;

			if (experienceVictoriesRemaining == 0) {
				experienceModifierPercent = 0;
			}
		}

		if (hasActiveGoldModifier()) {
			goldVictoriesRemaining--;

			if (goldVictoriesRemaining == 0) {
				goldModifierPercent = 0;
			}
		}
	}

	public boolean hasActiveExperienceModifier() {
		return experienceVictoriesRemaining > 0;
	}

	public boolean hasActiveGoldModifier() {
		return goldVictoriesRemaining > 0;
	}

	public int getExperienceModifierPercent() {
		return experienceModifierPercent;
	}

	public int getExperienceVictoriesRemaining() {
		return experienceVictoriesRemaining;
	}

	public int getGoldModifierPercent() {
		return goldModifierPercent;
	}

	public int getGoldVictoriesRemaining() {
		return goldVictoriesRemaining;
	}

}
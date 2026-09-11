package rpg.battle.rewards;

// Stores temporary party-wide reward modifiers.
public final class PartyRewardModifierState {

	private int experienceModifierPercent;
	private int experienceVictoriesRemaining;

	private int goldModifierPercent;
	private int goldVictoriesRemaining;

	public boolean activateExperienceModifier(int modifierPercent, int rewardedVictories) {
		validateActivation(modifierPercent, rewardedVictories);

		if (hasActiveExperienceModifier()) {
			return false;
		}

		experienceModifierPercent = modifierPercent;
		experienceVictoriesRemaining = rewardedVictories;

		return true;
	}

	public boolean activateGoldModifier(int modifierPercent, int rewardedVictories) {
		validateActivation(modifierPercent, rewardedVictories);

		if (hasActiveGoldModifier()) {
			return false;
		}

		goldModifierPercent = modifierPercent;
		goldVictoriesRemaining = rewardedVictories;

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

	private void validateActivation(int modifierPercent, int rewardedVictories) {
		if (modifierPercent <= 0) {
			throw new IllegalArgumentException("Reward modifier percentage must be positive.");
		}

		if (rewardedVictories <= 0) {
			throw new IllegalArgumentException("Reward modifier duration must be positive.");
		}
	}
}
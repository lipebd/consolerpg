package rpg.battle.rewards;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import rpg.characters.PlayableCharacter;

// Stores the reward modifiers applied to one battle victory.
public final class BattleRewardModifiers {

	private final int temporaryExperienceModifierPercent;
	private final int experienceVictoriesRemainingAfterReward;

	private final int temporaryGoldModifierPercent;
	private final int equipmentGoldModifierPercent;
	private final int goldVictoriesRemainingAfterReward;

	private final Map<PlayableCharacter, Integer> individualExperienceModifierPercents;

	public BattleRewardModifiers(
			int temporaryExperienceModifierPercent,
			int experienceVictoriesRemainingAfterReward,
			int temporaryGoldModifierPercent,
			int goldVictoriesRemainingAfterReward,
			int equipmentGoldModifierPercent,
			Map<PlayableCharacter, Integer> individualExperienceModifierPercents
	) {
		validateTemporaryModifier(
				temporaryExperienceModifierPercent,
				experienceVictoriesRemainingAfterReward,
				"Experience"
		);

		validateTemporaryModifier(temporaryGoldModifierPercent, goldVictoriesRemainingAfterReward, "Gold");

		if (equipmentGoldModifierPercent < 0) {
			throw new IllegalArgumentException("Equipment gold modifier cannot be negative.");
		}

		Objects.requireNonNull(individualExperienceModifierPercents, "Individual experience modifiers cannot be null.");

		Map<PlayableCharacter, Integer> copiedModifiers = new LinkedHashMap<>();

		for (Map.Entry<PlayableCharacter, Integer> entry : individualExperienceModifierPercents.entrySet()) {
			PlayableCharacter character = Objects.requireNonNull(entry.getKey(), "Modified character cannot be null.");

			Integer modifierPercent = Objects
					.requireNonNull(entry.getValue(), "Individual experience modifier cannot be null.");

			if (modifierPercent < 0) {
				throw new IllegalArgumentException("Individual experience modifier cannot be negative.");
			}

			copiedModifiers.put(character, modifierPercent);
		}

		this.temporaryExperienceModifierPercent = temporaryExperienceModifierPercent;

		this.experienceVictoriesRemainingAfterReward = experienceVictoriesRemainingAfterReward;

		this.temporaryGoldModifierPercent = temporaryGoldModifierPercent;

		this.equipmentGoldModifierPercent = equipmentGoldModifierPercent;

		this.goldVictoriesRemainingAfterReward = goldVictoriesRemainingAfterReward;

		this.individualExperienceModifierPercents = Map.copyOf(copiedModifiers);
	}

	public int getTemporaryExperienceModifierPercent() {
		return temporaryExperienceModifierPercent;
	}

	public int getExperienceVictoriesRemainingAfterReward() {
		return experienceVictoriesRemainingAfterReward;
	}

	public int getTemporaryGoldModifierPercent() {
		return temporaryGoldModifierPercent;
	}

	public int getEquipmentGoldModifierPercent() {
		return equipmentGoldModifierPercent;
	}

	public int getTotalGoldModifierPercent() {
		return Math.addExact(temporaryGoldModifierPercent, equipmentGoldModifierPercent);
	}

	public int getGoldVictoriesRemainingAfterReward() {
		return goldVictoriesRemainingAfterReward;
	}

	public int getIndividualExperienceModifierPercent(PlayableCharacter character) {
		Objects.requireNonNull(character, "Character cannot be null.");

		return individualExperienceModifierPercents.getOrDefault(character, 0);
	}

	public int getTotalExperienceModifierPercent(PlayableCharacter character) {
		return Math.addExact(temporaryExperienceModifierPercent, getIndividualExperienceModifierPercent(character));
	}

	public boolean hasTemporaryExperienceModifier() {
		return temporaryExperienceModifierPercent > 0;
	}

	public boolean hasEquipmentGoldModifier() {
		return equipmentGoldModifierPercent > 0;
	}

	public boolean hasTemporaryGoldModifier() {
		return temporaryGoldModifierPercent > 0;
	}

	public boolean hasIndividualExperienceModifier(PlayableCharacter character) {
		return getIndividualExperienceModifierPercent(character) > 0;
	}

	private void validateTemporaryModifier(int modifierPercent, int victoriesRemaining, String modifierName) {
		if (modifierPercent < 0) {
			throw new IllegalArgumentException(modifierName + " reward modifier cannot be negative.");
		}

		if (victoriesRemaining < 0) {
			throw new IllegalArgumentException(modifierName + " reward modifier duration cannot be negative.");
		}

		if (modifierPercent == 0 && victoriesRemaining != 0) {
			throw new IllegalArgumentException(
					modifierName + " reward modifier cannot have duration without a percentage."
			);
		}
	}

}
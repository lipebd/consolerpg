package rpg.battle;

import java.util.Objects;

import rpg.characters.GameCharacter;

// Calculates the chance of overcoming a target's resistance.
public final class EffectResistanceCalculator {

	private static final double RESISTANCE_BALANCE_FACTOR = 1.0;

	private static final int MAXIMUM_APPLICATION_CHANCE_PERCENT = 85;

	public int calculateApplicationChancePercent(GameCharacter applyingCharacter, GameCharacter target) {
		Objects.requireNonNull(applyingCharacter, "Applying character cannot be null.");

		Objects.requireNonNull(target, "Skill effect target cannot be null.");

		int applyingLuck = applyingCharacter.getLuck();
		int targetResistance = target.getResistance();

		double denominator = applyingLuck + targetResistance / RESISTANCE_BALANCE_FACTOR;

		if (denominator <= 0.0) {
			throw new IllegalStateException("Effect application denominator must be positive.");
		}

		double applicationChance = applyingLuck / denominator;

		int roundedChancePercent = (int) Math.round(applicationChance * 100.0);

		return Math.min(MAXIMUM_APPLICATION_CHANCE_PERCENT, roundedChancePercent);
	}
}
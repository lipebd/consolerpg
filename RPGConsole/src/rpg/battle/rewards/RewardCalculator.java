package rpg.battle.rewards;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

// Calculates rewards after additively combining percentage modifiers.
public final class RewardCalculator {

	private static final BigDecimal PERCENT_BASE = BigDecimal.valueOf(100);

	public int calculateModifiedReward(int baseReward, int... modifierPercents) {
		if (baseReward < 0) {
			throw new IllegalArgumentException("Base reward cannot be negative.");
		}

		Objects.requireNonNull(modifierPercents, "Reward modifier percentages cannot be null.");

		int totalModifierPercent = 0;

		for (int modifierPercent : modifierPercents) {
			if (modifierPercent < 0) {
				throw new IllegalArgumentException("Reward modifier percentage cannot be negative.");
			}

			totalModifierPercent = Math.addExact(totalModifierPercent, modifierPercent);
		}

		BigDecimal multiplier = PERCENT_BASE.add(BigDecimal.valueOf(totalModifierPercent)).divide(PERCENT_BASE);

		return BigDecimal.valueOf(baseReward).multiply(multiplier).setScale(0, RoundingMode.HALF_UP).intValueExact();
	}
}
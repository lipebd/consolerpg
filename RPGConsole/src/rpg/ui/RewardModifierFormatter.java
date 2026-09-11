package rpg.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import rpg.GameCurrency;

// Formats temporary reward modifiers for console screens.
public final class RewardModifierFormatter {

	private RewardModifierFormatter() {
	}

	public static String formatTemporarySummary(
			int experienceModifierPercent,
			int experienceVictoriesRemaining,
			int goldModifierPercent,
			int goldVictoriesRemaining
	) {
		validateValues(experienceModifierPercent, experienceVictoriesRemaining);

		validateValues(goldModifierPercent, goldVictoriesRemaining);

		List<String> formattedModifiers = new ArrayList<>();

		if (experienceModifierPercent > 0) {
			formattedModifiers.add(
					"M.EXP: " + formatMultiplier(experienceModifierPercent) + " (" + experienceVictoriesRemaining + ")"
			);
		}

		if (goldModifierPercent > 0) {
			formattedModifiers.add(
					"M." + GameCurrency.getName() + ": " + formatMultiplier(goldModifierPercent) + " ("
							+ goldVictoriesRemaining + ")"
			);
		}

		return String.join(" | ", formattedModifiers);
	}

	private static String formatMultiplier(int modifierPercent) {
		return BigDecimal.valueOf(100L + modifierPercent).divide(BigDecimal.valueOf(100)).stripTrailingZeros()
				.toPlainString() + "x";
	}

	private static void validateValues(int modifierPercent, int victoriesRemaining) {
		if (modifierPercent < 0 || victoriesRemaining < 0) {
			throw new IllegalArgumentException("Reward modifier values cannot be negative.");
		}
	}
}
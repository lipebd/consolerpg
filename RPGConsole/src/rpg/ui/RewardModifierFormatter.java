package rpg.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import rpg.GameCurrency;

//Formats reward modifiers for console screens.
public final class RewardModifierFormatter {

	private RewardModifierFormatter() {
	}

	public static String formatSummary(
			int experienceModifierPercent,
			int experienceVictoriesRemaining,
			boolean hasTemporaryExperienceModifier,
			int goldModifierPercent,
			int goldVictoriesRemaining,
			boolean hasTemporaryGoldModifier
	) {
		validateValues(experienceModifierPercent, experienceVictoriesRemaining);

		validateValues(goldModifierPercent, goldVictoriesRemaining);

		List<String> formattedModifiers = new ArrayList<>();

		addModifier(
				formattedModifiers,
				"M.EXP",
				experienceModifierPercent,
				experienceVictoriesRemaining,
				hasTemporaryExperienceModifier
		);

		addModifier(
				formattedModifiers,
				"M." + GameCurrency.getName(),
				goldModifierPercent,
				goldVictoriesRemaining,
				hasTemporaryGoldModifier
		);

		return String.join(" | ", formattedModifiers);
	}

	private static void addModifier(
			List<String> formattedModifiers,
			String label,
			int modifierPercent,
			int victoriesRemaining,
			boolean hasTemporaryModifier
	) {
		if (modifierPercent < 0) {
			return;
		}

		String formattedModifier = label + ": " + formatMultiplier(modifierPercent);

		if (hasTemporaryModifier) {
			formattedModifier += " (" + victoriesRemaining + ")";
		}

		formattedModifiers.add(formattedModifier);
	}

	public static String formatMultiplier(int modifierPercent) {
		if (modifierPercent < 0) {
			throw new IllegalArgumentException("Reward modifier percentage cannot be negative.");
		}

		return BigDecimal.valueOf(100L + modifierPercent).divide(BigDecimal.valueOf(100)).stripTrailingZeros()
				.toPlainString() + "x";
	}

	private static void validateValues(int modifierPercent, int victoriesRemaining) {
		if (modifierPercent < 0 || victoriesRemaining < 0) {
			throw new IllegalArgumentException("Reward modifier values cannot be negative.");
		}
	}
}
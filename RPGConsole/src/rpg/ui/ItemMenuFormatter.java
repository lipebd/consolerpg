package rpg.ui;

import rpg.items.RecoveryItem;

// Formats item information shared by inventory menus.
final class ItemMenuFormatter {

	static final int ITEM_NAME_WIDTH = 30;

	private ItemMenuFormatter() {
	}

	static String truncateName(String name) {
		if (name.length() <= ITEM_NAME_WIDTH) {
			return name;
		}

		int contentLength = ITEM_NAME_WIDTH - 3;

		String truncatedName = name.substring(0, contentLength).stripTrailing();

		return truncatedName + "...";
	}

	static String getRecoveryEffectSummary(RecoveryItem recoveryItem) {
		StringBuilder summary = new StringBuilder();

		appendEffect(summary, recoveryItem.getReviveHpRecovery(), "Revive com ", " HP");

		appendEffect(summary, recoveryItem.getHpRecovery(), "HP +", "");

		appendEffect(summary, recoveryItem.getMpRecovery(), "MP +", "");

		appendEffect(summary, recoveryItem.getSpRecovery(), "SP +", "");

		if (recoveryItem.purifiesStatusEffects()) {
			appendSeparator(summary);
			summary.append("Purifica");
		}

		return summary.toString();
	}

	private static void appendEffect(StringBuilder summary, int amount, String prefix, String suffix) {
		if (amount <= 0) {
			return;
		}

		appendSeparator(summary);

		summary.append(prefix).append(amount).append(suffix);
	}

	private static void appendSeparator(StringBuilder summary) {
		if (!summary.isEmpty()) {
			summary.append(" | ");
		}
	}
}
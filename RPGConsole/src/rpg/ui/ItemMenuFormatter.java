package rpg.ui;

import java.util.Arrays;
import java.util.stream.Collectors;

import rpg.characters.CharacterAttribute;
import rpg.items.RecoveryItem;
import rpg.items.RewardModifierItem;

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

	static String formatItemTableHeader() {
		return String.format("%-4s %-" + ITEM_NAME_WIDTH + "s %s", "", "Item", "Qtd.");
	}

	static String formatItemTableRow(int optionNumber, String itemName, int quantity) {
		return String.format("[%d]  %-" + ITEM_NAME_WIDTH + "s x%d", optionNumber, truncateName(itemName), quantity);
	}

	static String formatEmptyItemTableRow() {
		return String.format("     %-" + ITEM_NAME_WIDTH + "s %s", "--", "--");
	}

	static String getRecoveryEffectSummary(RecoveryItem recoveryItem) {
		StringBuilder summary = new StringBuilder();

		appendEffect(
				summary,
				recoveryItem.getReviveHpRecovery(),
				recoveryItem.getReviveHpRecoveryPercent(),
				"Revive com ",
				" HP",
				"% do HP máximo"
		);

		appendEffect(
				summary,
				recoveryItem.getHpRecovery(),
				recoveryItem.getHpRecoveryPercent(),
				"HP +",
				"",
				"% do máximo"
		);

		appendEffect(
				summary,
				recoveryItem.getMpRecovery(),
				recoveryItem.getMpRecoveryPercent(),
				"MP +",
				"",
				"% do máximo"
		);

		appendEffect(
				summary,
				recoveryItem.getSpRecovery(),
				recoveryItem.getSpRecoveryPercent(),
				"SP +",
				"",
				"% do máximo"
		);

		if (recoveryItem.purifiesStatusEffects()) {
			appendSeparator(summary);

			if (recoveryItem.getPurificationAttributes().size() == CharacterAttribute.values().length) {
				summary.append("Purifica todos os atributos");
			} else {
				String attributes = Arrays.stream(CharacterAttribute.values())
						.filter(recoveryItem.getPurificationAttributes()::contains).map(Enum::name)
						.collect(Collectors.joining(", "));

				summary.append("Purifica ").append(attributes);
			}
		}

		if (recoveryItem.hasAttributeStage()) {
			appendSeparator(summary);

			summary.append(recoveryItem.getStageAttribute()).append("↑".repeat(recoveryItem.getStageCount()));

			if (recoveryItem.getStageAttribute() == CharacterAttribute.VEL) {
				summary.append(" no próximo cálculo de rodada");
			} else {
				summary.append(" por ").append(recoveryItem.getStageDuration())
						.append(recoveryItem.getStageDuration() == 1 ? " turno" : " turnos");
			}
		}

		return summary.toString();
	}

	private static void appendEffect(
			StringBuilder summary,
			int amount,
			int percent,
			String prefix,
			String fixedSuffix,
			String percentSuffix
	) {
		if (amount <= 0 && percent <= 0) {
			return;
		}

		appendSeparator(summary);
		summary.append(prefix);

		if (percent > 0) {
			summary.append(percent).append(percentSuffix);
		} else {
			summary.append(amount).append(fixedSuffix);
		}
	}

	private static void appendSeparator(StringBuilder summary) {
		if (!summary.isEmpty()) {
			summary.append(" | ");
		}
	}

	static String getRewardModifierEffectSummary(RewardModifierItem item) {
		StringBuilder summary = new StringBuilder();

		if (item.getExperiencePercent() > 0) {
			summary.append("EXP +").append(item.getExperiencePercent()).append("%");
		}

		if (item.getGoldPercent() > 0) {
			appendSeparator(summary);
			summary.append("GOLD +").append(item.getGoldPercent()).append("%");
		}

		summary.append(" por ").append(item.getRewardedVictories()).append(" vitórias");

		return summary.toString();
	}
}
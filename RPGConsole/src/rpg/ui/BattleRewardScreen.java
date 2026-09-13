package rpg.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.math.BigDecimal;
import java.util.ArrayList;

import rpg.GameCurrency;
import rpg.battle.rewards.BattleRewardModifiers;
import rpg.battle.rewards.BattleRewards;
import rpg.battle.rewards.CharacterProgressionResult;
import rpg.characters.CharacterStats;
import rpg.items.Item;
import rpg.skills.SkillDefinition;

// Displays the rewards obtained after a battle victory.
public final class BattleRewardScreen {

	private static final int MAXIMUM_REWARD_LINE_LENGTH = 100;
	private final Scanner scanner;

	public BattleRewardScreen(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public void show(BattleRewards rewards) {
		Objects.requireNonNull(rewards, "Battle rewards cannot be null.");

		System.out.println("\n[RESULTADO DA BATALHA]");

		displayRewardModifiers(rewards);
		displayRewardSummary(rewards);
		displayLevelProgress(rewards);

		waitForContinue();
	}

	private void displayRewardModifiers(BattleRewards rewards) {
		BattleRewardModifiers modifiers = rewards.getRewardModifiers();

		String summary = RewardModifierFormatter.formatSummary(
				modifiers.getTemporaryExperienceModifierPercent(),
				modifiers.getExperienceVictoriesRemainingAfterReward(),
				modifiers.hasTemporaryExperienceModifier(),
				modifiers.getTotalGoldModifierPercent(),
				modifiers.getGoldVictoriesRemainingAfterReward(),
				modifiers.hasTemporaryGoldModifier()
		);

		if (!summary.isEmpty()) {
			System.out.println(summary);
		}
	}

	private void displayRewardSummary(BattleRewards rewards) {
		System.out.println("\nRecompensas:");

		System.out.printf("  %-7s+%d%n", "EXP:", rewards.getGlobalExperience());

		System.out.printf("  %-7s+%s%n", GameCurrency.getName() + ":", GameCurrency.format(rewards.getGold()));

		displayCollectedItems(rewards.getCollectedItems());
	}

	private void displayCollectedItems(Map<Item, Integer> collectedItems) {
		String linePrefix = "  Itens: ";
		String continuationPrefix = " ".repeat(linePrefix.length());

		if (collectedItems.isEmpty()) {
			System.out.println(linePrefix + "Nenhum");

			return;
		}

		StringBuilder currentLine = new StringBuilder(linePrefix);

		for (Map.Entry<Item, Integer> entry : collectedItems.entrySet()) {
			String formattedItem = entry.getKey().getName() + " x" + entry.getValue();

			boolean alreadyHasItem = currentLine.length() > linePrefix.length();

			String separator = alreadyHasItem ? ", " : "";

			boolean exceedsLineLength = currentLine.length() + separator.length()
					+ formattedItem.length() > MAXIMUM_REWARD_LINE_LENGTH;

			if (alreadyHasItem && exceedsLineLength) {
				System.out.println(currentLine + ",");

				currentLine = new StringBuilder(continuationPrefix);

				currentLine.append(formattedItem);

				continue;
			}

			currentLine.append(separator).append(formattedItem);
		}

		System.out.println(currentLine);
	}

	private void displayLevelProgress(BattleRewards rewards) {
		List<CharacterProgressionResult> results = rewards.getCharacterProgressionResults();

		boolean hasLevelUp = results.stream().anyMatch(result -> result.getLevelsGained() > 0);

		if (!hasLevelUp) {
			return;
		}

		System.out.println("\nProgressão:");

		boolean firstDisplayedCharacter = true;

		for (CharacterProgressionResult result : results) {
			int levelsGained = result.getLevelsGained();

			if (levelsGained <= 0) {
				continue;
			}

			if (!firstDisplayedCharacter) {
				System.out.println();
			}

			firstDisplayedCharacter = false;

			String levelWord = levelsGained == 1 ? "nível" : "níveis";

			String message = "- " + result.getCharacter().getName() + " subiu " + levelsGained + " " + levelWord + "! ["
					+ result.getPreviousLevel() + " -> " + result.getCurrentLevel() + "]";

			if (result.wereResourcesRestored()) {
				message += " | HP/MP/SP restaurados";
			}

			System.out.println(message);

			displayNaturalStatGrowth(result);
			displayNewlyUnlockedSkills(result);
		}
	}

	private void displayNaturalStatGrowth(CharacterProgressionResult result) {
		CharacterStats previousStats = result.getPreviousNaturalStats();

		CharacterStats currentStats = result.getCurrentNaturalStats();

		System.out.println(
				"  [" + formatGrowth("HP", previousStats.getMaxHp(), currentStats.getMaxHp()) + " | "
						+ formatGrowth("MP", previousStats.getMaxMp(), currentStats.getMaxMp()) + " | "
						+ formatGrowth("SP", previousStats.getMaxSp(), currentStats.getMaxSp()) + " | "
						+ formatGrowth("ATK", previousStats.getAttack(), currentStats.getAttack()) + " | "
						+ formatGrowth("DEF", previousStats.getDefense(), currentStats.getDefense()) + " | "
						+ formatGrowth("MAG", previousStats.getMagic(), currentStats.getMagic()) + " | "
						+ formatGrowth("RES", previousStats.getResistance(), currentStats.getResistance()) + " | "
						+ formatGrowth("VEL", previousStats.getSpeed(), currentStats.getSpeed()) + " | "
						+ formatGrowth("LCK", previousStats.getLuck(), currentStats.getLuck()) + "]"
		);
	}

	private void displayNewlyUnlockedSkills(CharacterProgressionResult result) {
		List<SkillDefinition> newlyUnlockedSkills = result.getNewlyUnlockedSkillDefinitions();

		if (newlyUnlockedSkills.isEmpty()) {
			return;
		}

		String characterName = result.getCharacter().getName();

		String skillNames = String.join(" | ", newlyUnlockedSkills.stream().map(SkillDefinition::getName).toList());

		if (newlyUnlockedSkills.size() == 1) {
			System.out.println("  " + characterName + " aprendeu " + skillNames + "!");

			return;
		}

		System.out.println("  " + characterName + " aprendeu novas habilidades: " + skillNames + ".");
	}

	private String formatGrowth(String label, int previousValue, int currentValue) {
		int growth = currentValue - previousValue;

		String formattedGrowth = growth >= 0 ? "+" + growth : Integer.toString(growth);

		return label + " " + formattedGrowth;
	}

	private void waitForContinue() {
		System.out.println("\nPressione ENTER para continuar...");

		System.out.print("> ");
		scanner.nextLine();
	}
}
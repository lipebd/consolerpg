package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.GameCurrency;
import rpg.battle.rewards.BattleRewardModifiers;
import rpg.battle.rewards.BattleRewards;
import rpg.battle.rewards.CharacterProgressionResult;
import rpg.characters.CharacterStats;
import rpg.items.Item;
import rpg.skills.SkillDefinition;

// Displays the rewards obtained after a battle victory.
public final class BattleRewardScreen {

	private static final int CONTENT_LINES_PER_PAGE = 12;

	private final ConsoleInput consoleInput;

	public BattleRewardScreen(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public void show(BattleRewards rewards) {
		Objects.requireNonNull(rewards, "Battle rewards cannot be null.");

		List<String> summaryLines = buildSummaryLines(rewards);
		List<String> progressionLines = buildProgressionLines(rewards);
		boolean hasProgression = !progressionLines.isEmpty();

		displayPages("Resultado da Batalha", summaryLines, hasProgression ? "Ver evolução" : "Continuar");

		if (hasProgression) {
			displayPages("Resultado da Batalha - Evolução", progressionLines, "Continuar");
		}
	}

	private List<String> buildSummaryLines(BattleRewards rewards) {
		List<String> lines = new ArrayList<>();

		lines.add("Recompensas:");
		lines.add("  EXP:   +" + rewards.getGlobalExperience());
		lines.add("  " + GameCurrency.getName() + ":  +" + GameCurrency.format(rewards.getGold()));

		addRewardModifiers(lines, rewards.getRewardModifiers());
		addCollectedItems(lines, rewards.getCollectedItems());

		lines.add("");
		lines.add("Grupo:");

		for (CharacterProgressionResult result : rewards.getCharacterProgressionResults()) {
			String levels = result.getLevelsGained() > 0 ? result.getPreviousLevel() + " → " + result.getCurrentLevel()
					: Integer.toString(result.getCurrentLevel());

			String entry = result.getCharacter().getName() + "  Nv " + levels + " | EXP +"
					+ result.getExperienceReceived();

			lines.addAll(ConsoleUI.wrapText(entry, "  "));
		}

		addDiscardedRewards(lines, rewards);

		return lines;
	}

	private void addRewardModifiers(List<String> lines, BattleRewardModifiers modifiers) {
		String summary = RewardModifierFormatter.formatSummary(
				modifiers.getTemporaryExperienceModifierPercent(),
				modifiers.getExperienceVictoriesRemainingAfterReward(),
				modifiers.hasTemporaryExperienceModifier(),
				modifiers.getTotalGoldModifierPercent(),
				modifiers.getGoldVictoriesRemainingAfterReward(),
				modifiers.hasTemporaryGoldModifier()
		);

		if (!summary.isEmpty()) {
			lines.addAll(ConsoleUI.wrapText(summary, "  "));
		}
	}

	private void addCollectedItems(List<String> lines, Map<Item, Integer> collectedItems) {
		if (collectedItems.isEmpty()) {
			lines.add("  Itens: Nenhum");
			return;
		}

		String items = String.join(
				", ",
				collectedItems.entrySet().stream().map(entry -> entry.getKey().getName() + " x" + entry.getValue())
						.toList()
		);

		lines.addAll(ConsoleUI.wrapText("Itens: " + items, "  "));
	}

	private void addDiscardedRewards(List<String> lines, BattleRewards rewards) {
		int discardedGold = rewards.getDiscardedGold();
		Map<Item, Integer> discardedItems = rewards.getDiscardedItems();

		if (discardedGold == 0 && discardedItems.isEmpty()) {
			return;
		}

		lines.add("");
		lines.add("Limites atingidos:");

		if (discardedGold > 0) {
			lines.addAll(
					ConsoleUI.wrapText(
							GameCurrency.getName() + " descartado: " + GameCurrency.format(discardedGold),
							"  "
					)
			);
		}

		for (Map.Entry<Item, Integer> entry : discardedItems.entrySet()) {
			lines.addAll(
					ConsoleUI.wrapText("Item descartado: " + entry.getKey().getName() + " x" + entry.getValue(), "  ")
			);
		}
	}

	private List<String> buildProgressionLines(BattleRewards rewards) {
		List<String> lines = new ArrayList<>();

		for (CharacterProgressionResult result : rewards.getCharacterProgressionResults()) {
			if (result.getLevelsGained() <= 0) {
				continue;
			}

			String characterLine = result.getCharacter().getName() + "  Nv " + result.getPreviousLevel() + " → "
					+ result.getCurrentLevel();

			if (result.wereResourcesRestored()) {
				characterLine += " | HP/MP/SP restaurados";
			}

			lines.addAll(ConsoleUI.wrapText(characterLine));

			CharacterStats previous = result.getPreviousNaturalStats();
			CharacterStats current = result.getCurrentNaturalStats();

			lines.addAll(
					ConsoleUI.wrapText(
							formatGrowth("HP", previous.getMaxHp(), current.getMaxHp()) + "  "
									+ formatGrowth("MP", previous.getMaxMp(), current.getMaxMp()) + "  "
									+ formatGrowth("SP", previous.getMaxSp(), current.getMaxSp()),
							"  "
					)
			);

			lines.addAll(
					ConsoleUI.wrapText(
							formatGrowth("ATK", previous.getAttack(), current.getAttack()) + "  "
									+ formatGrowth("DEF", previous.getDefense(), current.getDefense()) + "  "
									+ formatGrowth("MAG", previous.getMagic(), current.getMagic()) + "  "
									+ formatGrowth("RES", previous.getResistance(), current.getResistance()) + "  "
									+ formatGrowth("VEL", previous.getSpeed(), current.getSpeed()) + "  "
									+ formatGrowth("LCK", previous.getLuck(), current.getLuck()),
							"  "
					)
			);

			List<SkillDefinition> newSkills = result.getNewlyUnlockedSkillDefinitions();

			if (!newSkills.isEmpty()) {
				String skillNames = String.join(", ", newSkills.stream().map(SkillDefinition::getName).toList());

				lines.addAll(ConsoleUI.wrapText("Aprendeu: " + skillNames, "  "));
			}
		}

		return lines;
	}

	private String formatGrowth(String label, int previousValue, int currentValue) {
		int growth = currentValue - previousValue;
		String value = growth >= 0 ? "+" + growth : Integer.toString(growth);

		return label + " " + value;
	}

	private void displayPages(String heading, List<String> content, String finalAction) {
		int pageCount = Math.max(1, (content.size() + CONTENT_LINES_PER_PAGE - 1) / CONTENT_LINES_PER_PAGE);

		for (int page = 0; page < pageCount; page++) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(heading);
			System.out.println();

			int firstLine = page * CONTENT_LINES_PER_PAGE;
			int lastLine = Math.min(firstLine + CONTENT_LINES_PER_PAGE, content.size());

			for (int index = firstLine; index < lastLine; index++) {
				System.out.println(content.get(index));
			}

			for (int index = lastLine - firstLine; index < CONTENT_LINES_PER_PAGE; index++) {
				System.out.println();
			}

			System.out.println();

			String action = page < pageCount - 1 ? "Próxima página" : finalAction;

			System.out.println("[ENTER] " + action);
			System.out.print("> ");
			consoleInput.readLine();
		}
	}
}
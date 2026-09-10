package rpg.ui;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import rpg.GameCurrency;
import rpg.battle.rewards.BattleRewards;
import rpg.battle.rewards.CharacterProgressionResult;
import rpg.characters.CharacterStats;
import rpg.items.Item;
import rpg.skills.SkillDefinition;

// Displays the rewards obtained after a battle victory.
public final class BattleRewardScreen {

	private final Scanner scanner;

	public BattleRewardScreen(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public void show(BattleRewards rewards) {
		Objects.requireNonNull(rewards, "Battle rewards cannot be null.");

		System.out.println("\n[RESULTADO DA BATALHA]\n");

		displayExperienceRewards(rewards);

		System.out.println(GameCurrency.getName() + " recebido: +" + GameCurrency.format(rewards.getGold()));

		displayLevelProgress(rewards);
		displayCollectedItems(rewards);

		waitForContinue();
	}

	private void displayLevelProgress(BattleRewards rewards) {
		List<CharacterProgressionResult> results = rewards.getCharacterProgressionResults();

		boolean hasLevelUp = results.stream().anyMatch(result -> result.getLevelsGained() > 0);

		if (!hasLevelUp) {
			return;
		}

		for (CharacterProgressionResult result : results) {
			int levelsGained = result.getLevelsGained();

			if (levelsGained <= 0) {
				continue;
			}

			String levelWord = levelsGained == 1 ? "nível" : "níveis";

			String message = "\n- " + result.getCharacter().getName() + " subiu " + levelsGained + " " + levelWord + "! ["
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

	private void displayNewlyUnlockedSkills(
			CharacterProgressionResult result
	) {
		List<SkillDefinition> newlyUnlockedSkills =
				result.getNewlyUnlockedSkillDefinitions();

		if (newlyUnlockedSkills.isEmpty()) {
			return;
		}

		String characterName =
				result.getCharacter().getName();

		String skillNames = String.join(
				" | ",
				newlyUnlockedSkills
						.stream()
						.map(SkillDefinition::getName)
						.toList()
		);

		if (newlyUnlockedSkills.size() == 1) {
			System.out.println(
					"  "
							+ characterName
							+ " aprendeu "
							+ skillNames
							+ "!"
			);

			return;
		}

		System.out.println(
				"  "
						+ characterName
						+ " aprendeu novas habilidades: "
						+ skillNames
						+ "."
		);
	}

	private String formatGrowth(String label, int previousValue, int currentValue) {
		int growth = currentValue - previousValue;

		String formattedGrowth = growth >= 0 ? "+" + growth : Integer.toString(growth);

		return label + " " + formattedGrowth;
	}

	private void displayCollectedItems(BattleRewards rewards) {
		Map<Item, Integer> collectedItems = rewards.getCollectedItems();

		System.out.println("\nItens encontrados:");

		if (collectedItems.isEmpty()) {
			System.out.println("- Nenhum");
			return;
		}

		for (Map.Entry<Item, Integer> entry : collectedItems.entrySet()) {

			System.out.println("- " + entry.getKey().getName() + " x" + entry.getValue());
		}
	}

	private void waitForContinue() {
		System.out.println("\nPressione ENTER para continuar...");

		System.out.print("> ");
		scanner.nextLine();
	}

	private void displayExperienceRewards(BattleRewards rewards) {
		List<CharacterProgressionResult> results = rewards.getCharacterProgressionResults();

		if (results.size() == 1) {
			System.out.println("EXP recebida: +" + results.get(0).getExperienceReceived());

			return;
		}

		int firstExperience = results.get(0).getExperienceReceived();

		boolean sameExperienceForEveryone = results.stream()
				.allMatch(result -> result.getExperienceReceived() == firstExperience);

		if (sameExperienceForEveryone) {
			System.out.println("EXP recebida por aliado: +" + firstExperience);

			return;
		}

		System.out.println("EXP recebida:");

		for (CharacterProgressionResult result : results) {
			System.out.println("- " + result.getCharacter().getName() + ": +" + result.getExperienceReceived());
		}
	}
}
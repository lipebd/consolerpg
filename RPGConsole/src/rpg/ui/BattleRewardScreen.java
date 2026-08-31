package rpg.ui;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import rpg.battle.rewards.BattleRewards;
import rpg.battle.rewards.CharacterProgressionResult;
import rpg.items.Item;

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

		System.out.println("Ouro recebido: +" + rewards.getGold());

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

		System.out.println("");

		for (CharacterProgressionResult result : results) {
			int levelsGained = result.getLevelsGained();

			if (levelsGained <= 0) {
				continue;
			}

			String levelWord = levelsGained == 1 ? "nível" : "níveis";

			String message = "- " + result.getCharacter().getName() + " subiu " + levelsGained + " " + levelWord + "! ["
					+ result.getPreviousLevel() + " -> " + result.getCurrentLevel() + "]";

			if (result.wereResourcesRestored()) {
				message += " | HP/MP/SP restaurados";
			}

			System.out.println(message);
		}
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
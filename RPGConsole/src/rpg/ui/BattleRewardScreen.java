package rpg.ui;

import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import rpg.battle.rewards.BattleRewards;
import rpg.characters.Player;
import rpg.items.Item;

// Displays the rewards obtained after a battle victory.
public final class BattleRewardScreen {

    private final Scanner scanner;

    public BattleRewardScreen(Scanner scanner) {
        this.scanner = Objects.requireNonNull(
                scanner,
                "Scanner cannot be null."
        );
    }

    public void show(
            Player player,
            BattleRewards rewards
    ) {
        System.out.println("\n[RESULTADO DA BATALHA]");

        System.out.println(
                "EXP recebida: +"
                + rewards.getExperience()
        );

        System.out.println(
                "Ouro recebido: +"
                + rewards.getGold()
        );

        displayLevelProgress(player, rewards);
        displayCollectedItems(rewards);

        waitForContinue();
    }

    private void displayLevelProgress(
            Player player,
            BattleRewards rewards
    ) {
        int levelsGained = rewards.getLevelsGained();

        if (levelsGained <= 0) {
            return;
        }

        int previousLevel =
                player.getLevel() - levelsGained;

        String levelWord =
                levelsGained == 1
                ? "nível"
                : "níveis";

        System.out.println(
                player.getName() + " subiu "
                + levelsGained + " " + levelWord + "! ["
                + previousLevel + " -> "
                + player.getLevel() + "]"
        );
    }

    private void displayCollectedItems(
            BattleRewards rewards
    ) {
        Map<Item, Integer> collectedItems =
                rewards.getCollectedItems();

        System.out.println("\nItens encontrados:");

        if (collectedItems.isEmpty()) {
            System.out.println("- Nenhum");
            return;
        }

        for (Map.Entry<Item, Integer> entry
                : collectedItems.entrySet()) {

            System.out.println(
                    "- " + entry.getKey().getName()
                    + " x" + entry.getValue()
            );
        }
    }

    private void waitForContinue() {
        System.out.println(
                "\nPressione ENTER para continuar..."
        );

        System.out.print("> ");
        scanner.nextLine();
    }
}
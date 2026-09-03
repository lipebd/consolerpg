package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import rpg.world.scenarios.CombatScenarioAction;

// Displays the available actions for the current combat scenario.
public final class CombatScenarioMenu {

	private final Scanner scanner;

	public CombatScenarioMenu(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public Optional<CombatScenarioAction> selectAction(List<CombatScenarioAction> actions) {
		Objects.requireNonNull(actions, "Combat scenario actions cannot be null.");

		while (true) {
			System.out.println("\nO que deseja fazer?");

			for (int index = 0; index < actions.size(); index++) {
				CombatScenarioAction action = actions.get(index);

				if (index > 0 && startsNewActionGroup(action)) {
					System.out.println();
				}

				System.out.println("[" + (index + 1) + "] " + action);
			}

			System.out.println();
			System.out.println("[0] Encerrar jogo");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= actions.size()) {
					System.out.println("Opção inválida.");
					continue;
				}

				return Optional.of(actions.get(selectedIndex));

			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de uma ação.");
			}
		}
	}

	private boolean startsNewActionGroup(CombatScenarioAction action) {
		return action == CombatScenarioAction.TRAVEL || action == CombatScenarioAction.INVENTORY;
	}

}

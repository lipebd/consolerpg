package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import rpg.world.scenarios.ScenarioAction;

// Displays the available actions for the current scenario.
public final class ScenarioActionMenu {

	private final Scanner scanner;

	public ScenarioActionMenu(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public Optional<ScenarioAction> selectAction(List<ScenarioAction> actions) {
		Objects.requireNonNull(actions, "Scenario actions cannot be null.");

		while (true) {
			System.out.println("\nO que deseja fazer?");

			for (int index = 0; index < actions.size(); index++) {
				ScenarioAction action = actions.get(index);

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

	private boolean startsNewActionGroup(ScenarioAction action) {
		return action == ScenarioAction.TRAVEL || action == ScenarioAction.INVENTORY;
	}

}
package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioAction;

// Displays the available actions for the current scenario.
public final class ScenarioActionMenu {

	private final Scanner scanner;

	public ScenarioActionMenu(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public Optional<ScenarioAction> selectAction(Scenario scenario) {
		Objects.requireNonNull(scenario, "Scenario cannot be null.");

		List<ScenarioAction> actions = scenario.getAvailableActions();

		while (true) {
			System.out.println("\nO que deseja fazer?");

			for (int index = 0; index < actions.size(); index++) {

				System.out.print("[" + (index + 1) + "] " + actions.get(index) + " | ");
			}

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
}
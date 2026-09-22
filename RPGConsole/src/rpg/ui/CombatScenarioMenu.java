package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.WorldClock;
import rpg.world.scenarios.CombatScenarioAction;

// Displays the available actions for the current combat scenario.
public final class CombatScenarioMenu {

	private final ConsoleInput consoleInput;
	private final WorldClock worldClock;

	public CombatScenarioMenu(ConsoleInput consoleInput, WorldClock worldClock) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.worldClock = Objects.requireNonNull(worldClock, "World clock cannot be null.");
	}

    public Optional<CombatScenarioAction> selectAction(
            List<CombatScenarioAction> actions, Set<CombatScenarioAction> markedActions) {
        Objects.requireNonNull(actions, "Combat scenario actions cannot be null.");

        Objects.requireNonNull(markedActions, "Marked combat scenario actions cannot be null.");

		while (true) {
			System.out.println("\n" + WorldTimeFormatter.formatCurrentTime(worldClock));

			System.out.println("\nO que deseja fazer?");
			System.out.println();

			List<String> actionLabels =
			        actions.stream()
			                .map(
			                        action ->
			                                markedActions.contains(action)
			                                        ? "\u001B[33m[!]\u001B[0m " + action
			                                        : action.toString())
			                .toList();

			int secondRowStartIndex = actions.indexOf(CombatScenarioAction.INVENTORY);

			ConsoleUI.displayActionRows(actionLabels, secondRowStartIndex);

			System.out.println();
			System.out.println("[0] Encerrar jogo");
			System.out.print("> ");

			String choice = consoleInput.readLine();

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

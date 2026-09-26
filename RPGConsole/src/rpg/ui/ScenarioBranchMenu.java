package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.branches.ScenarioBranchDefinition;
import rpg.world.scenarios.ScenarioCatalog;

// Displays exploration branches and returns the selected branch.
public final class ScenarioBranchMenu {

	private final ConsoleInput consoleInput;
	private final ScenarioCatalog scenarioCatalog;

	public ScenarioBranchMenu(ConsoleInput consoleInput, ScenarioCatalog scenarioCatalog) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.scenarioCatalog = Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
	}

	public Optional<ScenarioBranchDefinition> open(
			String sourceScenarioName,
			List<ScenarioBranchDefinition> branches,
			Set<String> markedDestinationScenarioIds
	) {
		Objects.requireNonNull(branches, "Scenario branches cannot be null.");
		Objects.requireNonNull(markedDestinationScenarioIds, "Marked destination scenario IDs cannot be null.");

		if (branches.isEmpty()) {
			System.out.println("\nNão há outros caminhos disponíveis.");
			return Optional.empty();
		}

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(sourceScenarioName, "Bifurcação");

			System.out.println();
			System.out.println("Escolha o caminho que deseja seguir:");
			System.out.println();

			for (int index = 0; index < branches.size(); index++) {
				ScenarioBranchDefinition branch = branches.get(index);

				String marker = markedDestinationScenarioIds.contains(branch.getDestinationScenarioId())
						? "\u001B[33m[!]\u001B[0m "
						: "";

				String destinationName = scenarioCatalog.getById(branch.getDestinationScenarioId()).getName();

				System.out
						.println("[" + (index + 1) + "] " + marker + branch.getDisplayName() + ": " + destinationName);
			}

			System.out.println();
			System.out.println(feedbackMessage == null ? "" : feedbackMessage);
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= branches.size()) {
					feedbackMessage = "Caminho inválido.";
					continue;
				}

				ScenarioBranchDefinition selectedBranch = branches.get(selectedIndex);

				String destinationName = scenarioCatalog.getById(selectedBranch.getDestinationScenarioId()).getName();

				ConsoleUI.displayScenarioTransition(
						sourceScenarioName,
						destinationName,
						selectedBranch.getDescription(),
						consoleInput
				);

				return Optional.of(selectedBranch);
			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número de um caminho.";
			}
		}
	}
}
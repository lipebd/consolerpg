package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.branches.ScenarioBranchDefinition;

// Displays exploration branches and returns the selected branch.
public final class ScenarioBranchMenu {

	private final ConsoleInput consoleInput;

	public ScenarioBranchMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public Optional<ScenarioBranchDefinition> open(
	        List<ScenarioBranchDefinition> branches,
	        Set<String> markedDestinationScenarioIds) {
	    Objects.requireNonNull(
	            markedDestinationScenarioIds,
	            "Marked destination scenario IDs cannot be null.");

		if (branches.isEmpty()) {
			System.out.println("\nNão há outros caminhos disponíveis.");

			return Optional.empty();
		}

		while (true) {
			System.out.println("\nEscolha o caminho que deseja seguir:");

			for (int index = 0; index < branches.size(); index++) {
				ScenarioBranchDefinition branch = branches.get(index);
				String marker =
				        markedDestinationScenarioIds.contains(
				                branch.getDestinationScenarioId())
				                ? "\\u001B[33m[!]\\u001B[0m "
				                : "";

				System.out.println(
				        "[" + (index + 1) + "] "
				                + marker
				                + branch.getDisplayName());

				System.out.println("    " + branch.getDescription().replace("\n", "\n    "));
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= branches.size()) {
					System.out.println("Caminho inválido.");
					continue;
				}

				return Optional.of(branches.get(selectedIndex));

			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de um caminho.");
			}
		}
	}
}
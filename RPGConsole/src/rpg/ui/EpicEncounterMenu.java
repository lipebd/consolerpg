package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.enemies.EnemyDefinition;

// Displays an optional epic encounter and asks whether to fight.
public final class EpicEncounterMenu {

	private final ConsoleInput consoleInput;

	public EpicEncounterMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public boolean confirmEncounter(
			String scenarioName,
			EnemyDefinition epicDefinition
	) {
		Objects.requireNonNull(
				epicDefinition,
				"Epic enemy definition cannot be null."
		);

		return confirmEncounter(
				scenarioName,
				List.of(epicDefinition)
		);
	}

	public boolean confirmEncounter(
			String scenarioName,
			List<EnemyDefinition> epicDefinitions
	) {
		if (scenarioName == null || scenarioName.isBlank()) {
			throw new IllegalArgumentException(
					"Epic encounter scenario name cannot be empty."
			);
		}

		Objects.requireNonNull(
				epicDefinitions,
				"Epic enemy definitions cannot be null."
		);

		if (epicDefinitions.isEmpty()) {
			throw new IllegalArgumentException(
					"Epic enemy definitions cannot be empty."
			);
		}

		for (EnemyDefinition epicDefinition : epicDefinitions) {
			Objects.requireNonNull(
					epicDefinition,
					"Epic enemy definition cannot be null."
			);
		}

		String feedbackMessage = null;

		while (true) {
			displayEncounter(
					scenarioName,
					epicDefinitions,
					feedbackMessage
			);

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default ->
				feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void displayEncounter(String scenarioName, List<EnemyDefinition> epicDefinitions, String feedbackMessage) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(scenarioName, "Encontro Épico");

		for (EnemyDefinition epicDefinition : epicDefinitions) {
			System.out.println();
			System.out.println(epicDefinition.getName());
			ConsoleUI.displayWrappedText(epicDefinition.getDescription());
		}

		if (feedbackMessage != null) {
			System.out.println();
			ConsoleUI.displayWrappedText(feedbackMessage);
		}

		System.out.println();
		System.out.println("[1] Enfrentar");

		System.out.println();
		System.out.println("[0] Evitar confronto");
		System.out.print("> ");
	}
}
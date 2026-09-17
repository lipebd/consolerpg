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

	public boolean confirmEncounter(EnemyDefinition epicDefinition) {
		Objects.requireNonNull(epicDefinition, "Epic enemy definition cannot be null.");

		return confirmEncounter(List.of(epicDefinition));
	}

	public boolean confirmEncounter(List<EnemyDefinition> epicDefinitions) {
		Objects.requireNonNull(epicDefinitions, "Epic enemy definitions cannot be null.");

		if (epicDefinitions.isEmpty()) {
			throw new IllegalArgumentException("Epic enemy definitions cannot be empty.");
		}

		for (EnemyDefinition epicDefinition : epicDefinitions) {
			Objects.requireNonNull(epicDefinition, "Epic enemy definition cannot be null.");
		}

		displayEncounter(epicDefinitions);

		while (true) {
			System.out.println("\n[1] Enfrentar | [0] Evitar confronto");

			System.out.print("> ");

			String choice = consoleInput.readLine();

			switch (choice) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private void displayEncounter(List<EnemyDefinition> epicDefinitions) {
		System.out.println("\n[ENCONTRO ÉPICO]");

		for (EnemyDefinition epicDefinition : epicDefinitions) {
			System.out.println("\n[" + epicDefinition.getName() + "]");

			System.out.println(epicDefinition.getDescription());
		}
	}
}
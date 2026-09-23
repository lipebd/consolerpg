package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import rpg.narrative.NarrativeLocationDefinition;

// Displays one narrative location and its currently visible NPCs.
public final class NarrativeLocationMenu {

	private final ConsoleInput consoleInput;

	public NarrativeLocationMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public OptionalInt
			selectNpc(String cityName, NarrativeLocationDefinition locationDefinition, List<String> npcLabels) {
		if (cityName == null || cityName.isBlank()) {
			throw new IllegalArgumentException("City name cannot be empty.");
		}
		Objects.requireNonNull(locationDefinition, "Narrative location definition cannot be null.");

		Objects.requireNonNull(npcLabels, "NPC labels cannot be null.");

		if (npcLabels.isEmpty()) {
			displayEmptyLocation(cityName, locationDefinition);
			return OptionalInt.empty();
		}
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();

			displayLocationHeader(cityName, locationDefinition);

			for (int index = 0; index < npcLabels.size(); index++) {
				System.out.println("[" + (index + 1) + "] " + npcLabels.get(index));
			}
			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}
			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return OptionalInt.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= npcLabels.size()) {
					feedbackMessage = "Opção inválida.";
					continue;
				}

				return OptionalInt.of(selectedIndex);
			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número da opção desejada.";
			}
		}
	}

	private void displayEmptyLocation(String cityName, NarrativeLocationDefinition locationDefinition) {
		ConsoleUI.clearScreen();

		displayLocationHeader(cityName, locationDefinition);

		System.out.println("Não há ninguém aqui.");
		System.out.println();
		System.out.print("[Enter] Voltar");

		consoleInput.readLine();
	}

	private static void displayLocationHeader(
			String cityName,
			NarrativeLocationDefinition locationDefinition
	) {
		ConsoleUI.displayHeading(
				cityName,
				locationDefinition.getName()
		);

		System.out.println();
		ConsoleUI.displayWrappedText(
				locationDefinition.getDescription()
		);
		System.out.println();
	}
}

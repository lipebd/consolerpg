package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

// Displays functional city locations and returns the selected position.
public final class CityLocationMenu {

	private final ConsoleInput consoleInput;

	public CityLocationMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public OptionalInt selectLocation(List<String> locationNames, String cityName) {
		Objects.requireNonNull(locationNames, "City location names cannot be null.");

		if (locationNames.isEmpty()) {
			throw new IllegalArgumentException("City location names cannot be empty.");
		}
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(cityName);
			System.out.println();
			System.out.println("\nAnde deseja ir?");
			System.out.println();
			for (int index = 0; index < locationNames.size(); index++) {
				System.out.println(
						"[" + (index + 1) + "] " + locationNames.get(index)
				);
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

				if (selectedIndex < 0 || selectedIndex >= locationNames.size()) {
					feedbackMessage = "Local inválido.";
					continue;
				}

				return OptionalInt.of(selectedIndex);

			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número da opção desejada.";
			}
		}
	}
}
package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

// Displays functional and narrative city destinations and returns the selected position.
public final class CityLocationMenu {

	private static final int SERVICE_COLUMN_WIDTH = 36;
	private static final int COLUMN_GAP = 2;
	private static final int LOCATION_COLUMN_WIDTH = 37;

	private final ConsoleInput consoleInput;

	public CityLocationMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public OptionalInt selectLocation(List<String> locationNames, int serviceCount, String cityName) {
		Objects.requireNonNull(locationNames, "City location names cannot be null.");

		if (locationNames.isEmpty()) {
			throw new IllegalArgumentException("City location names cannot be empty.");
		}

		if (serviceCount < 0 || serviceCount > locationNames.size()) {
			throw new IllegalArgumentException("Service count is outside the location list.");
		}

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(cityName);
			System.out.println();
			System.out.println("Aonde deseja ir?");
			System.out.println();

			displayDestinations(locationNames, serviceCount);

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

	private static void displayDestinations(List<String> names, int serviceCount) {
		printColumns("Serviços", "Locais");

		int locationCount = names.size() - serviceCount;
		int rowCount = Math.max(serviceCount, locationCount);

		for (int row = 0; row < rowCount; row++) {
			List<String> serviceLines = row < serviceCount ? wrapOption(row + 1, names.get(row), SERVICE_COLUMN_WIDTH)
					: List.of("    --");

			List<String> locationLines = row < locationCount
					? wrapOption(serviceCount + row + 1, names.get(serviceCount + row), LOCATION_COLUMN_WIDTH)
					: List.of("--");

			int lineCount = Math.max(serviceLines.size(), locationLines.size());

			for (int line = 0; line < lineCount; line++) {
				String service = line < serviceLines.size() ? serviceLines.get(line) : "";
				String location = line < locationLines.size() ? locationLines.get(line) : "";

				if (ConsoleUI.getVisibleLength(service) > SERVICE_COLUMN_WIDTH
						|| ConsoleUI.getVisibleLength(location) > LOCATION_COLUMN_WIDTH) {
					if (!service.isEmpty()) {
						System.out.println(service);
					}
					if (!location.isEmpty()) {
						System.out.println(location);
					}
				} else {
					printColumns(service, location);
				}
			}
		}
	}

	private static List<String> wrapOption(int number, String name, int columnWidth) {
		String prefix = "[" + number + "] ";
		String continuationPrefix = " ".repeat(ConsoleUI.getVisibleLength(prefix));

		List<String> wrapped = ConsoleUI.wrapText(name, prefix, columnWidth);

		// The shared wrapper repeats its prefix. Continuations need blank indentation
		// here.
		if (wrapped.size() <= 1) {
			return wrapped;
		}

		java.util.ArrayList<String> lines = new java.util.ArrayList<>();
		lines.add(wrapped.get(0));

		for (int index = 1; index < wrapped.size(); index++) {
			lines.add(continuationPrefix + wrapped.get(index).substring(prefix.length()));
		}

		return List.copyOf(lines);
	}

	private static void printColumns(String service, String location) {
		int padding = Math.max(0, SERVICE_COLUMN_WIDTH - ConsoleUI.getVisibleLength(service) + COLUMN_GAP);

		System.out.println(service + " ".repeat(padding) + location);
	}
}
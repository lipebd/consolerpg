package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.WorldClock;
import rpg.world.scenarios.CityAction;

// Displays the actions currently available in a city.
public final class CityMenu {

	private final ConsoleInput consoleInput;
	private final WorldClock worldClock;

	public CityMenu(ConsoleInput consoleInput, WorldClock worldClock) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		this.worldClock = Objects.requireNonNull(worldClock, "World clock cannot be null.");
	}

	public Optional<CityAction> selectAction(
			String cityName,
			String cityDescription,
			boolean shouldDisplayDescription,
			List<CityAction> actions,
			Set<CityAction> markedActions
	) {
		if (cityName == null || cityName.isBlank()) {
			throw new IllegalArgumentException("City name cannot be empty.");
		}

		Objects.requireNonNull(cityDescription, "City description cannot be null.");
		Objects.requireNonNull(actions, "City actions cannot be null.");
		Objects.requireNonNull(markedActions, "Marked city actions cannot be null.");

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(cityName);

			if (shouldDisplayDescription) {
				System.out.println();
				ConsoleUI.displayWrappedText(cityDescription);
			}

			System.out.println();
			System.out.println(WorldTimeFormatter.formatCurrentTime(worldClock));

			System.out.println();
			System.out.println("O que deseja fazer?");

			System.out.println();

			List<String> actionLabels = actions.stream().map(
					action -> markedActions.contains(action) ? "\u001B[33m[!]\u001B[0m " + action : action.toString()
			).toList();

			int secondRowStartIndex = actions.indexOf(CityAction.INVENTORY);

			ConsoleUI.displayActionRows(actionLabels, secondRowStartIndex);

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[0] Encerrar jogo");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= actions.size()) {
					feedbackMessage = "Ação inválida.";
					continue;
				}

				return Optional.of(actions.get(selectedIndex));
			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}
}

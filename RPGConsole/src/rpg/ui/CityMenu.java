package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

	public Optional<CityAction> selectAction(String cityName, List<CityAction> actions) {
		Objects.requireNonNull(cityName, "City name cannot be null.");
		Objects.requireNonNull(actions, "City actions cannot be null.");

		while (true) {
			System.out.println("\n" + WorldTimeFormatter.formatCurrentTime(worldClock));

			System.out.println("\nO que deseja fazer?");
			System.out.println();

			int secondRowStartIndex = actions.indexOf(CityAction.INVENTORY);

			ConsoleUI.displayActionRows(actions, secondRowStartIndex);

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
					System.out.println("Ação inválida.");
					continue;
				}

				return Optional.of(actions.get(selectedIndex));

			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
			}
		}
	}
}

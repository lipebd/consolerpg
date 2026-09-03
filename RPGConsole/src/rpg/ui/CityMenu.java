package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import rpg.world.scenarios.CityAction;

// Displays the actions currently available in a city.
public final class CityMenu {

	private final Scanner scanner;

	public CityMenu(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public Optional<CityAction> selectAction(String cityName, List<CityAction> actions) {
		Objects.requireNonNull(cityName, "City name cannot be null.");
		Objects.requireNonNull(actions, "City actions cannot be null.");

		while (true) {
			System.out.println("\nO que deseja fazer em " + cityName + "?");

			for (int index = 0; index < actions.size(); index++) {
				CityAction action = actions.get(index);

				if (index > 0 && startsNewActionGroup(action)) {
					System.out.println();
				}

				System.out.println("[" + (index + 1) + "] " + action);
			}

			System.out.println();
			System.out.println("[0] Encerrar jogo");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

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
				System.out.println("Digite o número de uma ação.");
			}
		}
	}

	private boolean startsNewActionGroup(CityAction action) {
		return action == CityAction.INVENTORY;
	}
}

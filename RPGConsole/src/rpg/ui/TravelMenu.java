package rpg.ui;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.travel.TravelProgress;
import rpg.world.travel.TravelRouteDefinition;

// Displays unlocked travel routes and returns the selected scenario.
public final class TravelMenu {

	private final Scanner scanner;
	private final ScenarioCatalog scenarioCatalog;
	private final TravelProgress travelProgress;

	public TravelMenu(Scanner scanner, ScenarioCatalog scenarioCatalog, TravelProgress travelProgress) {
		this.scanner = scanner;
		this.scenarioCatalog = scenarioCatalog;
		this.travelProgress = travelProgress;
	}

	public Optional<Scenario> open(Scenario currentScenario) {
		List<TravelRouteDefinition> routes = travelProgress.getUnlockedRoutesFrom(currentScenario.getId());

		if (routes.isEmpty()) {
			System.out.println("\nNão há destinos possíveis.");
			return Optional.empty();
		}

		while (true) {
			System.out.println("\nEscolha seu destino:");

			for (int index = 0; index < routes.size(); index++) {
				TravelRouteDefinition route = routes.get(index);

				System.out.println("[" + (index + 1) + "] " + route.getDisplayName());

				System.out.println("    " + route.getDescription().replace("\n", "\n    "));
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= routes.size()) {
					System.out.println("Destino inválido.");
					continue;
				}

				TravelRouteDefinition selectedRoute = routes.get(selectedIndex);

				Scenario destination = scenarioCatalog.getById(selectedRoute.getDestinationScenarioId());

				return Optional.of(destination);

			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
			}
		}
	}
}
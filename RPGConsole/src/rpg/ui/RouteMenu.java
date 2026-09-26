package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.travel.TravelProgress;
import rpg.world.travel.TravelRouteDefinition;

// Displays unlocked travel routes and returns the selected scenario.
public final class RouteMenu {

	private final ConsoleInput consoleInput;
	private final TravelProgress travelProgress;
	private final ScenarioCatalog scenarioCatalog;

	public RouteMenu(ConsoleInput consoleInput, TravelProgress travelProgress, ScenarioCatalog scenarioCatalog) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.travelProgress = Objects.requireNonNull(travelProgress, "Travel progress cannot be null.");
		this.scenarioCatalog = Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
	}

	public Optional<TravelRouteDefinition>
			open(String sourceScenarioId, String sourceScenarioName, Set<String> markedDestinationScenarioIds) {
		Objects.requireNonNull(markedDestinationScenarioIds, "Marked destination scenario IDs cannot be null.");
		List<TravelRouteDefinition> routes = travelProgress.getUnlockedRoutesFrom(sourceScenarioId);

		if (routes.isEmpty()) {
			System.out.println("\nNão há destinos possíveis.");
			return Optional.empty();
		}
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(sourceScenarioName, "Rotas");

			System.out.println();
			System.out.println("Aonde deseja ir?");
			System.out.println();
			for (int index = 0; index < routes.size(); index++) {
				TravelRouteDefinition route = routes.get(index);

				String marker = markedDestinationScenarioIds.contains(route.getDestinationScenarioId())
						? "\u001B[33m[!]\u001B[0m "
						: "";

				String destinationName = scenarioCatalog.getById(route.getDestinationScenarioId()).getName();

				String label = route.getDisplayName() + ": " + destinationName;

				System.out.println("[" + (index + 1) + "] " + marker + label);
			}
			System.out.println();
			System.out.println(feedbackMessage == null ? "" : feedbackMessage);
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= routes.size()) {
					feedbackMessage = "Destino inválido.";
					continue;
				}

				TravelRouteDefinition selectedRoute = routes.get(selectedIndex);
				ConsoleUI.displayScenarioTransition(
				        sourceScenarioName,
				        scenarioCatalog.getById(selectedRoute.getDestinationScenarioId()).getName(),
				        selectedRoute.getDescription(),
				        consoleInput
				);
				return Optional.of(selectedRoute);

			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número do destino desejado.";
			}
		}
	}
}
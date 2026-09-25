package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.travel.TravelProgress;
import rpg.world.travel.TravelRouteDefinition;

// Displays unlocked travel routes and returns the selected scenario.
public final class RouteMenu {

	private final ConsoleInput consoleInput;
	private final TravelProgress travelProgress;

	public RouteMenu(
            ConsoleInput consoleInput,
            TravelProgress travelProgress) {
    this.consoleInput = consoleInput;
    this.travelProgress = travelProgress;
}

	public Optional<TravelRouteDefinition> open(
			String sourceScenarioId,
			String sourceScenarioName,
			Set<String> markedDestinationScenarioIds
	) {
        Objects.requireNonNull(
                markedDestinationScenarioIds, "Marked destination scenario IDs cannot be null.");
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

			for (int index = 0; index < routes.size(); index++) {
				TravelRouteDefinition route = routes.get(index);

				String marker =
				        markedDestinationScenarioIds.contains(route.getDestinationScenarioId())
				                ? "\u001B[33m[!]\u001B[0m "
				                : "";

				System.out.println(
				        "[" + (index + 1) + "] " + marker + route.getDisplayName());

				ConsoleUI.displayWrappedText(route.getDescription(), "    ");
			}
            if (feedbackMessage != null) {
                System.out.println();
                ConsoleUI.displayWrappedText(feedbackMessage);
            }
            System.out.println();
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

				return Optional.of(selectedRoute);

			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número do destino desejado.";
			}
		}
	}
}
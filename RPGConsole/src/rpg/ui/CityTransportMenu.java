package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.transport.CityTransportDefinition;

// Displays paid city destinations and controls transport payment.
public final class CityTransportMenu {

	private static final int DESTINATION_NAME_WIDTH = 26;

	private final Party party;
	private final Scanner scanner;
	private final ScenarioCatalog scenarioCatalog;

	public CityTransportMenu(Party party, Scanner scanner, ScenarioCatalog scenarioCatalog) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
		this.scenarioCatalog = Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
	}

	public Optional<CityTransportDefinition>
			open(List<CityTransportDefinition> destinations, int destinationSlotCount) {
		Objects.requireNonNull(destinations, "City transport destinations cannot be null.");

		if (destinationSlotCount < destinations.size()) {
			throw new IllegalArgumentException(
					"Destination slot count cannot be lower than the available destination count."
			);
		}

		if (destinations.isEmpty()) {
			return Optional.empty();
		}

		String feedbackMessage = null;

		while (true) {
			displayMenu(destinations, destinationSlotCount, feedbackMessage);

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= destinations.size()) {
					feedbackMessage = "Destino inválido.";
					continue;
				}

				CityTransportDefinition selectedDestination = destinations.get(selectedIndex);

				if (party.getGold() < selectedDestination.getPrice()) {
					feedbackMessage = "As moedas do grupo não são suficientes para realizar esta viagem.";
					continue;
				}

				if (!confirmTravel(selectedDestination)) {
					feedbackMessage = null;
					continue;
				}

				if (!party.spendGold(selectedDestination.getPrice())) {
					throw new IllegalStateException("Party gold changed before city transport payment.");
				}

				return Optional.of(selectedDestination);

			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número de um destino.";
			}
		}
	}

	private void
			displayMenu(List<CityTransportDefinition> destinations, int destinationSlotCount, String feedbackMessage) {
		System.out.println("\nEscolha seu destino:");

		for (int index = 0; index < destinations.size(); index++) {
			CityTransportDefinition definition = destinations.get(index);
			Scenario destination = scenarioCatalog.getById(definition.getDestinationScenarioId());

			System.out.printf(
					"[%d] %-" + DESTINATION_NAME_WIDTH + "s %s%n",
					index + 1,
					destination.getName(),
					GameCurrency.format(definition.getPrice())
			);
		}

		int emptySlotCount = destinationSlotCount - destinations.size();

		for (int index = 0; index < emptySlotCount; index++) {
			System.out.printf("    %-" + DESTINATION_NAME_WIDTH + "s --%n", "--");
		}

		System.out.println("\n" + GameCurrency.getName() + " disponível: " + GameCurrency.format(party.getGold()));

		if (feedbackMessage != null) {
			System.out.println();
			System.out.println(feedbackMessage);
		}

		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private boolean confirmTravel(CityTransportDefinition destinationDefinition) {
		Scenario destination = scenarioCatalog.getById(destinationDefinition.getDestinationScenarioId());

		while (true) {
			System.out.println(
					"\nViajar para " + destination.getName() + " por "
							+ GameCurrency.format(destinationDefinition.getPrice()) + "?"
			);

			System.out.println();
			System.out.println("[1] Confirmar");
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

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
}
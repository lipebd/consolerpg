package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.transport.CityTransportDefinition;

// Displays paid city destinations and controls transport payment.
public final class CityTransportMenu {

	private static final int DESTINATION_NAME_WIDTH = 26;

	private final Party party;
	private final ConsoleInput consoleInput;
	private final ScenarioCatalog scenarioCatalog;

	public CityTransportMenu(Party party, ConsoleInput consoleInput, ScenarioCatalog scenarioCatalog) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.scenarioCatalog = Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
	}

	public Optional<CityTransportDefinition> open(
			String sourceScenarioName,
			List<CityTransportDefinition> destinations,
			int destinationSlotCount,
			Set<String> markedDestinationScenarioIds
	) {
		Objects.requireNonNull(destinations, "City transport destinations cannot be null.");

		Objects.requireNonNull(markedDestinationScenarioIds, "Marked destination scenario IDs cannot be null.");

		if (destinationSlotCount < destinations.size()) {
			throw new IllegalArgumentException(
					"Destination slot count cannot be lower than the available destination count."
			);
		}

		if (destinations.isEmpty()) {
			return Optional.empty();
		}

		Objects.requireNonNull(markedDestinationScenarioIds, "Marked destination scenario IDs cannot be null.");

		String feedbackMessage = null;

		while (true) {
			displayMenu(
					sourceScenarioName,
					destinations,
					destinationSlotCount,
					markedDestinationScenarioIds,
					feedbackMessage
			);

			String choice = consoleInput.readTrimmedLine();

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

				if (!confirmTravel(sourceScenarioName, selectedDestination)) {
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

	private void displayMenu(
			String sourceScenarioName,
			List<CityTransportDefinition> destinations,
			int destinationSlotCount,
			Set<String> markedDestinationScenarioIds,
			String feedbackMessage
	) {
		ConsoleUI.displayHeading(sourceScenarioName, "Viagem");

		System.out.println();
		System.out.println("Aonde deseja ir?");

		for (int index = 0; index < destinations.size(); index++) {
			CityTransportDefinition definition = destinations.get(index);
			Scenario destination = scenarioCatalog.getById(definition.getDestinationScenarioId());

			String marker = markedDestinationScenarioIds.contains(definition.getDestinationScenarioId())
					? "\u001B[33m[!]\u001B[0m "
					: "";

			String destinationLabel = marker + destination.getName();
			String visibleLabel = destinationLabel.replaceAll("\u001B\\[[0-9;]*m", "");
			int padding = Math.max(0, DESTINATION_NAME_WIDTH - visibleLabel.length());

			System.out.printf(
					"[%d] %s%s %s%n",
					index + 1,
					destinationLabel,
					" ".repeat(padding),
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
			ConsoleUI.displayWrappedText(feedbackMessage);
		}

		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private boolean confirmTravel(String sourceScenarioName, CityTransportDefinition destinationDefinition) {
		Scenario destination = scenarioCatalog.getById(destinationDefinition.getDestinationScenarioId());
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(sourceScenarioName, "Viagem");

			System.out.println();
			System.out.println(
					"Viajar para " + destination.getName() + " por "
							+ GameCurrency.format(destinationDefinition.getPrice()) + "?"
			);
			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}
			System.out.println();
			System.out.println("[1] Confirmar");
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}
}
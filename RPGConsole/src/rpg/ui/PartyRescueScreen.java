package rpg.ui;

import java.util.Objects;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.world.scenarios.City;
import rpg.world.scenarios.CombatScenario;

// Displays the rescue flow after the entire party is defeated.
public final class PartyRescueScreen {

	private final ConsoleInput consoleInput;

	public PartyRescueScreen(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public void show(Party party, CombatScenario defeatedScenario, City rescueDestination, int rescueCost) {
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(defeatedScenario, "Defeated scenario cannot be null.");
		Objects.requireNonNull(rescueDestination, "Rescue destination cannot be null.");

		if (rescueCost < 0) {
			throw new IllegalArgumentException("Rescue cost cannot be negative.");
		}
		ConsoleUI.clearScreen();
		System.out.println("[DERROTA]");

		System.out.println("\nPressione ENTER para continuar...");

		System.out.print("> ");
		consoleInput.readLine();
		ConsoleUI.displayWrappedText(defeatedScenario.getRescueDescription());

		System.out.println();
		System.out.println("Destino: " + rescueDestination.getName());

		displayRestorationMessage(party);

		System.out.println("Custo do resgate e dos cuidados: " + GameCurrency.format(rescueCost));
		System.out.println();
		System.out.println("Pressione ENTER para continuar...");
		System.out.print("> ");
		consoleInput.readLine();
	}

	private void displayRestorationMessage(Party party) {
		System.out.println();

		if (party.size() == 1) {
			ConsoleUI.displayWrappedText(
					party.getLeader().getName() + " foi resgatado e teve suas saúde restabelecida."
			);

			return;
		}

		ConsoleUI.displayWrappedText("Todos os integrantes foram resgatados e tiveram a saúde restabelecida.");
	}
}

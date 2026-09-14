package rpg.ui;

import java.util.Objects;
import java.util.Scanner;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.world.scenarios.City;
import rpg.world.scenarios.CombatScenario;

// Displays the rescue flow after the entire party is defeated.
public final class PartyRescueScreen {

	private final Scanner scanner;

	public PartyRescueScreen(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public void show(Party party, CombatScenario defeatedScenario, City rescueDestination, int rescueCost) {
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(defeatedScenario, "Defeated scenario cannot be null.");
		Objects.requireNonNull(rescueDestination, "Rescue destination cannot be null.");

		if (rescueCost < 0) {
			throw new IllegalArgumentException("Rescue cost cannot be negative.");
		}

		System.out.println("\n[DERROTA]");

		System.out.println("\n" + defeatedScenario.getRescueDescription());

		System.out.println("\nDestino do resgate: " + rescueDestination.getName());

		displayRestorationMessage(party);

		System.out.println("Custo do resgate e dos cuidados: " + GameCurrency.format(rescueCost));

		waitForContinue();
	}

	private void displayRestorationMessage(Party party) {
		if (party.size() == 1) {
			System.out.println(
					"\n" + party.getLeader().getName() + " foi resgatado e teve suas forças completamente restauradas."
			);

			return;
		}

		System.out.println(
				"\nTodos os integrantes do grupo foram resgatados " + "e tiveram suas forças completamente restauradas."
		);
	}

	private void waitForContinue() {
		System.out.println("\nPressione ENTER para continuar...");

		System.out.print("> ");
		scanner.nextLine();
	}
}

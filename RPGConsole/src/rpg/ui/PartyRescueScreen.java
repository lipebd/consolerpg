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
		ConsoleUI.clearScreen();
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(defeatedScenario, "Defeated scenario cannot be null.");
		Objects.requireNonNull(rescueDestination, "Rescue destination cannot be null.");

		if (rescueCost < 0) {
			throw new IllegalArgumentException("Rescue cost cannot be negative.");
		}
		String rescueMessage = defeatedScenario.getRescueDescription()
				+ "\n\nDestino: "
				+ rescueDestination.getName()
				+ "\n\n"
				+ buildRestorationMessage(party)
				+ "\n\nCusto do resgate e dos cuidados: "
				+ GameCurrency.format(rescueCost);

		ConsoleUI.displayScenarioTransition(
				defeatedScenario.getName(),
				"Derrota",
				rescueMessage,
				consoleInput
		);

		System.out.println();
		System.out.println("Pressione ENTER para continuar...");
		System.out.print("> ");
		consoleInput.readLine();
	}

	private String buildRestorationMessage(Party party) {
		if (party.size() == 1) {
			return party.getLeader().getName()
					+ " foi resgatado e teve sua saúde restabelecida.";
		}

		return "Todos os integrantes foram resgatados e tiveram a saúde restabelecida.";
	}
}

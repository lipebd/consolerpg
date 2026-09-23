package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.WorldClock;
import rpg.world.exploration.ExplorationSession;
import rpg.world.scenarios.CombatScenarioAction;

// Displays the available actions for the current combat scenario.
public final class CombatScenarioMenu {

	private final ConsoleInput consoleInput;
	private final WorldClock worldClock;

	public CombatScenarioMenu(ConsoleInput consoleInput, WorldClock worldClock) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.worldClock = Objects.requireNonNull(worldClock, "World clock cannot be null.");
	}

	public Optional<CombatScenarioAction> selectAction(
			String scenarioName,
			String scenarioDescription,
			ExplorationSession explorationSession,
			boolean shouldDisplayDescription,
			boolean dungeonBossObstacleVisible,
			int partySize,
			List<String> explorationMessages,
			List<CombatScenarioAction> actions,
			Set<CombatScenarioAction> markedActions
	) {
		if (scenarioName == null || scenarioName.isBlank()) {
			throw new IllegalArgumentException("Combat scenario name cannot be empty.");
		}

		Objects.requireNonNull(scenarioDescription, "Combat scenario description cannot be null.");
		Objects.requireNonNull(explorationSession, "Exploration session cannot be null.");
		Objects.requireNonNull(actions, "Combat scenario actions cannot be null.");
		Objects.requireNonNull(markedActions, "Marked combat scenario actions cannot be null.");
		Objects.requireNonNull(explorationMessages, "Exploration messages cannot be null.");

		if (partySize <= 0) {
			throw new IllegalArgumentException("Party size must be positive.");
		}

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(scenarioName, getExplorationHeadingQualifier(explorationSession));

			if (shouldDisplayDescription) {
				System.out.println();
				ConsoleUI.displayWrappedText(scenarioDescription);
			}

			if (dungeonBossObstacleVisible) {
				String target = partySize == 1 ? "você" : "o grupo";

				System.out.println();
				ConsoleUI.displayWrappedText("Uma criatura se coloca entre " + target + " e o caminho de volta.");
			}

			System.out.println();
			System.out.println(WorldTimeFormatter.formatCurrentTime(worldClock));

			if (!explorationMessages.isEmpty()) {
				System.out.println();

				for (String message : explorationMessages) {
					ConsoleUI.displayWrappedText(message);
				}
			}

			System.out.println();
			System.out.println("O que deseja fazer?");

			System.out.println();

			List<String> actionLabels = actions.stream().map(
					action -> markedActions.contains(action) ? "\u001B[33m[!]\u001B[0m " + action : action.toString()
			).toList();

			int secondRowStartIndex = actions.indexOf(CombatScenarioAction.INVENTORY);

			ConsoleUI.displayActionRows(actionLabels, secondRowStartIndex);

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

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
					feedbackMessage = "Ação inválida.";
					continue;
				}

				return Optional.of(actions.get(selectedIndex));
			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}

	private static String getExplorationHeadingQualifier(
			ExplorationSession explorationSession
	) {
		if (explorationSession.isComplete()) {
			return "Exploração Concluída";
		}

		return "Exploração "
				+ explorationSession.getCurrentPosition()
				+ "/"
				+ explorationSession.getSettings().getLength();
	}
}

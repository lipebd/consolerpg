package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rpg.world.WorldClock;
import rpg.world.exploration.ExplorationSession;
import rpg.world.scenarios.CombatScenarioAction;

// Displays the available actions for the current combat scenario.
public final class CombatScenarioMenu {

	private static final long EXPLORATION_CHARACTER_DELAY_MILLIS = 25;
	private static final long EXPLORATION_MESSAGE_PAUSE_MILLIS = 1200;

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
			Set<CombatScenarioAction> markedActions,
			int firstAnimatedMessageIndex,
			int narratedExplorationPosition
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
		int animateFromIndex = firstAnimatedMessageIndex;

		while (true) {
			boolean animatingExploration = narratedExplorationPosition >= 1 && animateFromIndex >= 0
					&& animateFromIndex < explorationMessages.size();

			String headingQualifier = animatingExploration
					? "Exploração " + narratedExplorationPosition + "/" + explorationSession.getSettings().getLength()
					: getExplorationHeadingQualifier(explorationSession);

			displayExplorationHeading(scenarioName, headingQualifier);

			boolean showDescriptionPreview = shouldDisplayDescription && explorationMessages.isEmpty()
					&& !dungeonBossObstacleVisible;

			if (showDescriptionPreview) {
				ConsoleUI.displayMapDescriptionPreview(scenarioDescription, 3);

				int descriptionLines = Math.min(3, ConsoleUI.wrapText(scenarioDescription).size());

				for (int line = descriptionLines; line < 3; line++) {
					System.out.println();
				}
			} else {
				List<String> sceneMessages = new ArrayList<>();

				if (dungeonBossObstacleVisible) {
					String target = partySize == 1 ? "você" : "o grupo";

					sceneMessages.add("Uma criatura se coloca entre " + target + " e o caminho de volta.");
				}

				sceneMessages.addAll(explorationMessages);

				int animatedIndex = animateFromIndex;

				if (animatedIndex >= 0 && dungeonBossObstacleVisible) {
					animatedIndex++;
				}

				displayExplorationMessages(scenarioName, headingQualifier, sceneMessages, animatedIndex);
			}
			if (animatingExploration) {
				animateFromIndex = -1;
				continue;
			}
			System.out.println();
			System.out.println(WorldTimeFormatter.formatCurrentTime(worldClock));
			System.out.println();
			System.out.println("O que deseja fazer?");

			System.out.println();

			List<String> actionLabels = actions.stream().map(
					action -> markedActions.contains(action) ? "\u001B[33m[!]\u001B[0m " + action : action.toString()
			).toList();

			int secondRowStartIndex = actions.indexOf(CombatScenarioAction.INVENTORY);

			ConsoleUI.displayActionRows(actionLabels, secondRowStartIndex);

			System.out.println(feedbackMessage == null ? "" : feedbackMessage);

			if (showDescriptionPreview) {
				System.out.println("[0] Encerrar jogo");
			} else {
				ConsoleUI.displayMapMenuFooter();
			}

			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			animateFromIndex = -1;

			if (choice.equals("0")) {
				return Optional.empty();
			}

			if ((showDescriptionPreview && choice.equalsIgnoreCase("M"))
					|| (!showDescriptionPreview && choice.equalsIgnoreCase("D"))) {
				ConsoleUI.displayMapDescription(scenarioName, scenarioDescription, consoleInput);
				feedbackMessage = null;
				continue;
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

	private static String getExplorationHeadingQualifier(ExplorationSession explorationSession) {
		if (explorationSession.isComplete()) {
			return "Exploração Concluída";
		}

		return "Exploração " + explorationSession.getCurrentPosition() + "/"
				+ explorationSession.getSettings().getLength();
	}

	private void displayExplorationMessages(
			String scenarioName,
			String headingQualifier,
			List<String> messages,
			int firstAnimatedMessageIndex
	) {
		List<String> lines = new ArrayList<>();
		int firstAnimatedLineIndex = -1;

		for (int index = 0; index < messages.size(); index++) {
			if (index == firstAnimatedMessageIndex) {
				firstAnimatedLineIndex = lines.size();
			}

			lines.addAll(ConsoleUI.wrapText(messages.get(index)));
		}

		boolean hasAnimation = firstAnimatedLineIndex >= 0 && firstAnimatedLineIndex < lines.size();

		if (!hasAnimation) {
			int firstVisible = Math.max(0, lines.size() - 3);

			for (int index = firstVisible; index < lines.size(); index++) {
				System.out.println(lines.get(index));
			}

			for (int index = lines.size() - firstVisible; index < 3; index++) {
				System.out.println();
			}

			return;
		}

		consoleInput.discardPendingInput();

		try {
			boolean interrupted = false;

			for (int index = 0; index < lines.size(); index++) {
				if (index >= 3) {
					displayExplorationHeading(scenarioName, headingQualifier);
					System.out.println(lines.get(index - 2));
					System.out.println(lines.get(index - 1));
				}

				if (index < firstAnimatedLineIndex) {
					System.out.println(lines.get(index));
					continue;
				}

				String line = lines.get(index);

				for (int offset = 0; offset < line.length();) {
					int codePoint = line.codePointAt(offset);

					System.out.print(new String(Character.toChars(codePoint)));
					System.out.flush();

					if (consoleInput.pollLine(EXPLORATION_CHARACTER_DELAY_MILLIS).isPresent()) {
						interrupted = true;
						break;
					}

					offset += Character.charCount(codePoint);
				}

				if (interrupted) {
					break;
				}

				System.out.println();

				if (consoleInput.pollLine(EXPLORATION_MESSAGE_PAUSE_MILLIS).isPresent()) {
					interrupted = true;
					break;
				}
			}

			if (interrupted) {
				// The terminal echoed Enter; redraw the complete final window.
				displayExplorationHeading(scenarioName, headingQualifier);

				int firstVisible = Math.max(0, lines.size() - 3);

				for (int index = firstVisible; index < lines.size(); index++) {
					System.out.println(lines.get(index));
				}
			}

			for (int index = Math.min(lines.size(), 3); index < 3; index++) {
				System.out.println();
			}
		} finally {
			consoleInput.discardPendingInput();
		}
	}

	public void displayExplorationEncounter(
			String scenarioName,
			ExplorationSession explorationSession,
			List<String> messages
	) {
		String headingQualifier = getExplorationHeadingQualifier(explorationSession);
		displayExplorationHeading(scenarioName, headingQualifier);

		displayExplorationMessages(scenarioName, headingQualifier, messages, 0);

		System.out.println();
		System.out.println(WorldTimeFormatter.formatCurrentTime(worldClock));
	}

	private static void displayExplorationHeading(String scenarioName, String headingQualifier) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(scenarioName);
		System.out.println();
		System.out.println(headingQualifier.toUpperCase(Locale.ROOT));
		System.out.println();
	}
}

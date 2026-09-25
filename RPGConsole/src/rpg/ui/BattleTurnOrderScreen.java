package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.battle.BattleTurnEntry;
import rpg.battle.BattleTurnOrder;
import rpg.battle.BattleType;

// Displays the remaining action order of the current battle round.
public final class BattleTurnOrderScreen {

	private static final int MAXIMUM_VISIBLE_ENTRIES = 5;

	public void show(
			BattleTurnOrder turnOrder,
			int roundNumber,
			String scenarioName,
			BattleType battleType,
			ConsoleInput consoleInput
	) {
		Objects.requireNonNull(turnOrder, "Battle turn order cannot be null.");
		Objects.requireNonNull(scenarioName, "Scenario name cannot be null.");
		Objects.requireNonNull(battleType, "Battle type cannot be null.");
		Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		if (roundNumber < 1) {
			throw new IllegalArgumentException("Round number must be positive.");
		}

		String feedbackMessage = null;

		while (true) {
			display(turnOrder, roundNumber, scenarioName, battleType, feedbackMessage);

			if ("0".equals(consoleInput.readTrimmedLine())) {
				return;
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private void display(
			BattleTurnOrder turnOrder,
			int roundNumber,
			String scenarioName,
			BattleType battleType,
			String feedbackMessage
	) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(scenarioName, BattleScreen.headingQualifier(battleType) + " - Ordem");

		System.out.println();
		System.out.println("Rodada " + roundNumber + ": próximas ações");
		System.out.println();

		List<BattleTurnEntry> visibleEntries = turnOrder.getVisibleEntries(MAXIMUM_VISIBLE_ENTRIES);

		int bodyLinesPrinted = 0;

		for (int index = 0; index < visibleEntries.size(); index++) {
			BattleTurnEntry entry = visibleEntries.get(index);

			System.out.println((index + 1) + ". " + getPositionMarker(index) + entry.getDisplayName());
			bodyLinesPrinted++;
		}

		int remainingActionCount = turnOrder.getRemainingActionCount();
		int hiddenActionCount = remainingActionCount - visibleEntries.size();

		if (hiddenActionCount > 0) {
			while (bodyLinesPrinted < MAXIMUM_VISIBLE_ENTRIES) {
				System.out.println();
				bodyLinesPrinted++;
			}

			String actionWord = hiddenActionCount == 1 ? "ação" : "ações";

			System.out.println();
			System.out.println("... e mais " + hiddenActionCount + " " + actionWord + " nesta rodada.");
			System.out.println();
			bodyLinesPrinted += 3;
		}

		System.out.println((remainingActionCount + 1) + ". PRÓXIMA RODADA");
		bodyLinesPrinted++;

		while (bodyLinesPrinted < 10) {
			System.out.println();
			bodyLinesPrinted++;
		}

		System.out.println("[0] Voltar");
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.print("> ");
	}

	private String getPositionMarker(int index) {
		return switch (index) {
		case 0 -> "[AGORA] ";
		case 1 -> "[A SEGUIR] ";
		default -> "";
		};
	}
}
package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.battle.BattleTurnEntry;
import rpg.battle.BattleTurnOrder;

// Displays the remaining action order of the current battle round.
public final class BattleTurnOrderScreen {

	private static final int MAXIMUM_VISIBLE_ENTRIES = 5;

	public void show(BattleTurnOrder turnOrder, int roundNumber) {
		Objects.requireNonNull(turnOrder, "Battle turn order cannot be null.");

		if (roundNumber < 1) {
			throw new IllegalArgumentException("Round number must be positive.");
		}

		System.out.println("\n[RODADA #" + roundNumber + "]\n");

		List<BattleTurnEntry> visibleEntries = turnOrder.getVisibleEntries(MAXIMUM_VISIBLE_ENTRIES);

		if (visibleEntries.isEmpty()) {
			System.out.println("Nenhuma ação restante nesta rodada.");

			displayNextRoundSeparator();
			return;
		}

		for (int index = 0; index < visibleEntries.size(); index++) {

			BattleTurnEntry entry = visibleEntries.get(index);

			String marker = getPositionMarker(index);

			System.out.println((index + 1) + ". " + marker + entry.getDisplayName());
		}

		int hiddenActionCount = turnOrder.getRemainingActionCount() - visibleEntries.size();

		if (hiddenActionCount > 0) {
			String actionWord = hiddenActionCount == 1 ? "ação" : "ações";

			System.out.println("\n... e mais " + hiddenActionCount + " " + actionWord + " nesta rodada.");
		}

		displayNextRoundSeparator();
	}

	private String getPositionMarker(int index) {
		return switch (index) {
		case 0 -> "[AGORA] ";
		case 1 -> "[A SEGUIR] ";
		default -> "";
		};
	}

	private void displayNextRoundSeparator() {
		System.out.println("\n---------- PRÓXIMA RODADA ----------");
	}
}
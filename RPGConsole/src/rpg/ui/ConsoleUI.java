package rpg.ui;

import java.util.List;
import java.util.Objects;

// Provides shared console presentation utilities.
public final class ConsoleUI {

	private static final int ACTION_MENU_COLUMN_WIDTH = 25;

	private ConsoleUI() {
	}

	public static void clearScreen() {
		// \033[H moves the cursor to the top left; \033[2J clears the screen
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	public static void displayActionRows(List<?> actions, int secondRowStartIndex) {
		Objects.requireNonNull(actions, "Actions cannot be null.");

		if (secondRowStartIndex <= 0 || secondRowStartIndex >= actions.size()) {
			throw new IllegalArgumentException("Second action row must start inside the action list.");
		}

		displayActionRow(actions, 0, secondRowStartIndex);
		displayActionRow(actions, secondRowStartIndex, actions.size());
	}

	private static void displayActionRow(List<?> actions, int firstIndex, int lastIndex) {
		for (int index = firstIndex; index < lastIndex; index++) {
			String option = "[" + (index + 1) + "] " + actions.get(index);

			if (index == lastIndex - 1) {
				System.out.println(option);
			} else {
				System.out.printf("%-" + ACTION_MENU_COLUMN_WIDTH + "s", option);
			}
		}
	}

	public static void pause(long milliseconds) {
		if (milliseconds < 0) {
			throw new IllegalArgumentException("Pause duration cannot be negative.");
		}

		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}
}
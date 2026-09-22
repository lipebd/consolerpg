package rpg.ui;

import java.util.List;
import java.util.Objects;

// Provides shared console presentation utilities.
public final class ConsoleUI {

	private static final int ACTION_MENU_COLUMN_WIDTH = 25;
	private static final int TEXT_COLUMN_WIDTH = 75;

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
				String visibleOption = option.replaceAll("\u001B\\[[0-9;]*m", "");
				int padding = Math.max(0, ACTION_MENU_COLUMN_WIDTH - visibleOption.length());

				System.out.print(option + " ".repeat(padding));
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

	public static void pause(long milliseconds, ConsoleInput consoleInput) {
		Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		consoleInput.beginDiscardingInput();

		try {
			pause(milliseconds);
		} finally {
			consoleInput.endDiscardingInput();
		}
	}

	public static void displayIgnoredEndOfInput() {
		System.out.println();
		System.out.println("Ctrl+Z ignorado.");
		System.out.print("> ");
	}

	public static void displayWrappedText(String text) {
	    Objects.requireNonNull(
	            text,
	            "Displayed text cannot be null.");

	    String[] paragraphs = text.split("\n", -1);

	    for (String paragraph : paragraphs) {
	        if (paragraph.isBlank()) {
	            System.out.println();
	            continue;
	        }

	        displayWrappedParagraph(paragraph);
	    }
	}

	private static void displayWrappedParagraph(String paragraph) {
	    String[] words = paragraph.trim().split("\\s+");
	    StringBuilder currentLine = new StringBuilder();

	    for (String word : words) {
	        if (currentLine.isEmpty()) {
	            currentLine.append(word);
	            continue;
	        }

	        int resultingLength =
	                currentLine.codePointCount(
	                        0,
	                        currentLine.length())
	                        + 1
	                        + word.codePointCount(
	                                0,
	                                word.length());

	        if (resultingLength <= TEXT_COLUMN_WIDTH) {
	            currentLine.append(' ').append(word);
	            continue;
	        }

	        System.out.println(currentLine);
	        currentLine.setLength(0);
	        currentLine.append(word);
	    }

	    if (!currentLine.isEmpty()) {
	        System.out.println(currentLine);
	    }
	}

}

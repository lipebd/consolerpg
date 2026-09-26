package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Locale;

// Provides shared console presentation utilities.
public final class ConsoleUI {

	private static final int ACTION_MENU_COLUMN_WIDTH = 25;
	private static final int TEXT_COLUMN_WIDTH = 75;
	private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\u001B\\[[0-9;]*m");

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
				int padding = Math.max(0, ACTION_MENU_COLUMN_WIDTH - getVisibleLength(option));

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

	static void animateLine(String line, long characterDelayMillis) {
		Objects.requireNonNull(line, "Animated line cannot be null.");

		for (int offset = 0; offset < line.length();) {
			int codePoint = line.codePointAt(offset);

			System.out.print(new String(Character.toChars(codePoint)));
			System.out.flush();
			pause(characterDelayMillis);

			offset += Character.charCount(codePoint);
		}
	}

	public static void displayIgnoredEndOfInput() {
		System.out.println();
		System.out.println("Ctrl+Z ignorado.");
		System.out.print("> ");
	}

	public static void displayWrappedText(String text) {
		displayWrappedText(text, "");
	}

	public static void displayWrappedText(String text, String linePrefix) {
		for (String line : wrapText(text, linePrefix)) {
			System.out.println(line);
		}
	}

	public static void displayHeading(String heading) {
		System.out.println("[" + normalizeHeadingComponent(heading, "Heading") + "]");
	}

	public static void displayHeading(String context, String qualifier) {
		System.out.println(
				"[" + normalizeHeadingComponent(context, "Heading context") + " - "
						+ normalizeHeadingComponent(qualifier, "Heading qualifier") + "]"
		);
	}

	private static String normalizeHeadingComponent(String component, String componentName) {
		Objects.requireNonNull(component, componentName + " cannot be null.");

		if (component.isBlank()) {
			throw new IllegalArgumentException(componentName + " cannot be empty.");
		}

		return component.trim().toUpperCase(Locale.ROOT);
	}

	static List<String> wrapText(String text) {
		return wrapText(text, "");
	}

	static List<String> wrapText(String text, String linePrefix) {
		return wrapText(text, linePrefix, TEXT_COLUMN_WIDTH);
	}

	static List<String> wrapText(String text, String linePrefix, int columnWidth) {
		Objects.requireNonNull(text, "Displayed text cannot be null.");
		Objects.requireNonNull(linePrefix, "Line prefix cannot be null.");

		int availableWidth = columnWidth - getVisibleLength(linePrefix);

		if (availableWidth <= 0) {
			throw new IllegalArgumentException("Line prefix must leave visible space for text.");
		}

		List<String> wrappedLines = new ArrayList<>();
		String[] paragraphs = text.split("\n", -1);

		for (String paragraph : paragraphs) {
			if (paragraph.isBlank()) {
				wrappedLines.add("");
				continue;
			}

			appendWrappedParagraph(wrappedLines, paragraph, linePrefix, availableWidth);
		}

		return List.copyOf(wrappedLines);
	}

	private static void
			appendWrappedParagraph(List<String> wrappedLines, String paragraph, String linePrefix, int availableWidth) {
		String[] words = paragraph.strip().split("\\s+");
		StringBuilder currentLine = new StringBuilder();

		for (String word : words) {
			if (currentLine.isEmpty()) {
				currentLine.append(word);
				continue;
			}

			int resultingLength = getVisibleLength(currentLine.toString()) + 1 + getVisibleLength(word);

			if (resultingLength <= availableWidth) {
				currentLine.append(' ').append(word);
				continue;
			}

			wrappedLines.add(linePrefix + currentLine);
			currentLine.setLength(0);
			currentLine.append(word);
		}

		if (!currentLine.isEmpty()) {
			wrappedLines.add(linePrefix + currentLine);
		}
	}

	static int getVisibleLength(String text) {
		String visibleText = ANSI_ESCAPE_PATTERN.matcher(text).replaceAll("");

		return visibleText.codePointCount(0, visibleText.length());
	}

	public static void displayMapDescriptionPreview(String description, int maxLines) {
		if (maxLines <= 0) {
			throw new IllegalArgumentException("Preview line count must be positive.");
		}

		List<String> lines = wrapText(description);
		int visibleLines = Math.min(maxLines, lines.size());

		for (int index = 0; index < visibleLines; index++) {
			String line = lines.get(index);

			if (index == maxLines - 1 && lines.size() > maxLines) {
				String suffix = "... [M] Mais";

				while (getVisibleLength(line + suffix) > TEXT_COLUMN_WIDTH) {
					int lastSpace = line.lastIndexOf(' ');

					if (lastSpace < 0) {
						line = "";
						break;
					}

					line = line.substring(0, lastSpace);
				}

				System.out.println(line + suffix);
			} else {
				System.out.println(line);
			}
		}
	}

	static void displayMapDescription(String mapName, String description, ConsoleInput consoleInput) {
		Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		List<String> lines = wrapText(description);
		int linesPerPage = 12;

		for (int start = 0; start < lines.size(); start += linesPerPage) {
			clearScreen();
			displayHeading(mapName, "Descrição do mapa");
			System.out.println();

			int end = Math.min(start + linesPerPage, lines.size());

			for (int index = start; index < end; index++) {
				System.out.println(lines.get(index));
			}

			for (int index = end - start; index < linesPerPage; index++) {
				System.out.println();
			}

			System.out.println();
			System.out.println(end < lines.size() ? "[ENTER] Próxima página" : "[ENTER] Voltar");
			System.out.print("> ");
			consoleInput.readLine();
		}
	}

	static void displayMapMenuFooter() {
		String exitOption = "[0] Encerrar jogo";
		int padding = Math.max(1, ACTION_MENU_COLUMN_WIDTH - getVisibleLength(exitOption));

		System.out.println(exitOption + " ".repeat(padding) + "[D] Descrição do mapa");
	}

	public static void displayScenarioTransition(String sourceName, String destinationName, String description, ConsoleInput consoleInput) {
		Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		List<String> lines = wrapText(description);
		boolean interrupted = false;

		consoleInput.discardPendingInput();
		clearScreen();
		displayHeading(sourceName, destinationName);
		System.out.println();

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index);
			for (int offset = 0; offset < line.length();) {
				int codePoint = line.codePointAt(offset);

				System.out.print(new String(Character.toChars(codePoint)));
				System.out.flush();

				if (consoleInput.pollLine(25).isPresent()) {
					interrupted = true;
					break;
				}

				offset += Character.charCount(codePoint);
			}

			if (interrupted) {
				break;
			}

			System.out.println();

			if (index < lines.size() - 1 && consoleInput.pollLine(1200).isPresent()) {
				interrupted = true;
				break;
			}
		}

		if (interrupted) {
			// Remove the echoed Enter and show the complete transition text.
			clearScreen();
			displayHeading(sourceName, destinationName);
			System.out.println();

			for (String line : lines) {
				System.out.println(line);
			}
		}
		consoleInput.pollLine(2500);
		consoleInput.discardPendingInput();
	}
}

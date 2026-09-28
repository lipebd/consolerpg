package rpg.ui;

import java.util.List;
import java.util.Objects;

// Guides the player to the terminal size expected by the console interface.
public final class TerminalSetupScreen {

	private static final int SCREEN_WIDTH = 77;
	private static final int CONTENT_WIDTH = SCREEN_WIDTH - 2;

	private TerminalSetupScreen() {
	}

	public static void show(ConsoleInput consoleInput) {
		Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		List<String> contentLines = List.of(
				"",
				"",
				center("AJUSTE DO TERMINAL"),
				"",
				" Para uma experiência ideal, redimensione a janela do terminal até que",
				" toda esta moldura fique visível, sem quebras ou rolagem.",
				"",
				"",
				" Após ajustar, pressione ENTER para continuar.",
				"",
				"",
				"",
				"",
				"",
				""
		);

		ConsoleUI.clearScreen();
		System.out.println("-".repeat(SCREEN_WIDTH));

		for (String contentLine : contentLines) {
			displayContentLine(contentLine);
		}

		System.out.print("-".repeat(SCREEN_WIDTH));
		consoleInput.readLine();
	}

	private static void displayContentLine(String content) {
		if (content.length() > CONTENT_WIDTH) {
			throw new IllegalArgumentException("Terminal setup content exceeds the screen width.");
		}

		System.out.println("|" + content + " ".repeat(CONTENT_WIDTH - content.length()) + "|");
	}

	private static String center(String content) {
		int leadingSpaces = (CONTENT_WIDTH - content.length()) / 2;
		return " ".repeat(leadingSpaces) + content;
	}
}

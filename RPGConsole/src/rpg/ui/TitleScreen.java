package rpg.ui;

// Displays the provisional title screen.
public final class TitleScreen {

	private static final int SCREEN_WIDTH = 77;
	private static final String SEPARATOR = "=";
	private static final String GAME_TITLE = "RPG CONSOLE";
	private static final String GAME_VERSION = "v9.1.0";

	private TitleScreen() {
	}

	public static void show(ConsoleInput consoleInput) {
		ConsoleUI.clearScreen();
		StringBuilder screenSeparator = new StringBuilder();
		for (int i = 1; i <= SCREEN_WIDTH; i++) {
			screenSeparator.append(SEPARATOR);
		}
		System.out.println(screenSeparator);
		displayCentralizedMessage(screenSeparator, GAME_TITLE, StringColor.CYAN, true);
		displayCentralizedMessage(screenSeparator, GAME_VERSION, StringColor.CYAN, true);
		System.out.println(screenSeparator);
		System.out.println();
		System.out.println("Desenvolvido por:");
		System.out.println();
		System.out.println("César Nardino");
		System.out.println("Felipe Bachtchen");
		System.out.println("Matheus Leandro");
		System.out.println();
		displayCentralizedMessage(screenSeparator, "PRESSIONE ENTER PARA INICIAR", StringColor.DEFAULT, true);
		System.out.println();
		System.out.println(screenSeparator);
		displayCentralizedMessage(screenSeparator, "Unicuritiba", StringColor.DEFAULT, true);
		displayCentralizedMessage(screenSeparator, "2026", StringColor.DEFAULT, true);
		System.out.print(screenSeparator);
		consoleInput.readLine();
	}

	private static void displayCentralizedMessage(
			StringBuilder referenceString,
			String messageToPrint,
			StringColor color,
			boolean newLine
	) {
		String lineBreak = newLine ? "\n" : "";
		int paddingSize = Math.max(0, (referenceString.length() - messageToPrint.length()) / 2);
		System.out.print(" ".repeat(paddingSize));
		System.out.print(color.colorCode() + messageToPrint + "\u001B[0m" + lineBreak);
	}

	public enum StringColor {
		BLACK("\u001B[30m"),
		RED("\u001B[31m"),
		GREEN("\u001B[32m"),
		YELLOW("\u001B[33m"),
		BLUE("\u001B[34m"),
		MAGENTA("\u001B[35m"),
		CYAN("\u001B[36m"),
		WHITE("\u001B[37m"),
		BABY_YELLOW("\u001B[93m"),
		BABY_BLUE("\u001B[96m"),
		BACK_CYAN("\u001B[46m"),
		DEFAULT("\u001B[0m");

		private final String code;

		StringColor(String code) {
			this.code = code;
		}

		public String colorCode() {
			return code;
		}
	}
}

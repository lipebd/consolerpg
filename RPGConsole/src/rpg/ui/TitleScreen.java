package rpg.ui;

// Displays the provisional title screen.
public final class TitleScreen {

    private static final String GAME_TITLE = "RPG CONSOLE - V8";

    private TitleScreen() {}

    public static void show(ConsoleInput consoleInput) {
        ConsoleUI.clearScreen();
    	String gameTitle = "             \u001B[36m" + GAME_TITLE + "\u001B[0m";
        System.out.println("========================================");

        System.out.println(gameTitle);

        System.out.println("========================================");

        System.out.println("\nDesenvolvido por:");
        System.out.println("César Nardino");
        System.out.println("Felipe Bachtchen");
        System.out.println("Matheus Leandro");
        System.out.println("\nPRESSIONE ENTER PARA INICIAR");

        System.out.print("> ");
        consoleInput.readLine();
    }
}

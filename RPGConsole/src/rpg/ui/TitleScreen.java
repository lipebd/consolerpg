package rpg.ui;


// Displays the provisional title screen.
public final class TitleScreen {

    private static final String GAME_TITLE =
            "RPG CONSOLE — V4";

    private TitleScreen() {
    }

    public static void show(ConsoleInput consoleInput) {
        System.out.println(
                "========================================"
        );

        System.out.println(
                "             " + GAME_TITLE
        );

        System.out.println(
                "========================================"
        );

        System.out.println("\nDesenvolvido por:");
        System.out.println("César Nardino");
        System.out.println("Felipe Bachtchen");
        System.out.println("Matheus Leandro");
        System.out.println(
                "\nPRESSIONE ENTER PARA INICIAR"
        );

        System.out.print("> ");
        consoleInput.readLine();
    }
}
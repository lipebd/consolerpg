package rpg.ui;

import java.util.Scanner;

// Displays the provisional title screen.
public final class TitleScreen {

    private static final String GAME_TITLE =
            "RPG CONSOLE — V2";

    private TitleScreen() {
    }

    public static void show(Scanner scanner) {
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
        scanner.nextLine();
    }
}
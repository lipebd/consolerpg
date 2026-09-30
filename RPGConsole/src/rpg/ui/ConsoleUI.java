package rpg.ui;

// Provides shared console presentation utilities.
public final class ConsoleUI {

    private static final int CLEAR_SCREEN_LINES = 50;

    private ConsoleUI() {
    }

    public static void clearScreen() {
        for (int line = 0;
                line < CLEAR_SCREEN_LINES;
                line++) {

            System.out.println();
        }
    }
}
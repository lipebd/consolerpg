package rpg.ui;

import rpg.characters.PlayableCharacter;

// Displays the game-over screen when the player is defeated.
public final class GameOverScreen {

    private GameOverScreen() {
    }

    public static void show(PlayableCharacter player) {
        System.out.println(
                "\n========================================"
        );

        System.out.println("               GAME OVER");

        System.out.println(
                "========================================"
        );

        System.out.println(
                "\n" + player.getName() + " foi derrotado."
        );

        System.out.println(
                "\nObrigado por testar a V1!"
        );
    }
}
package rpg.ui;

import java.util.Objects;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;

// Displays the game over screen after the entire party is defeated.
public final class GameOverScreen {

	private GameOverScreen() {
	}

	public static void show(Party party) {
		Objects.requireNonNull(party, "Party cannot be null.");

		System.out.println("\n========================================");

		System.out.println("               GAME OVER");

		System.out.println("========================================");

		displayDefeatMessage(party);

		System.out.println("\nObrigado por testar esta versão do jogo!");
	}

	private static void displayDefeatMessage(Party party) {
		if (party.size() == 1) {
			PlayableCharacter character = party.getLeader();

			System.out.println("\n" + character.getName() + " foi derrotado.");

			return;
		}

		System.out.println("\nTodos os integrantes do grupo " + "foram derrotados.");
	}
}
package rpg.ui;

import java.util.Objects;

import rpg.characters.PlayableCharacter;
import rpg.classes.PlayableClassDefinition;

// Presents the mandatory action that confirms ally recruitment.
public final class AllyRecruitmentMenu {

	private final ConsoleInput consoleInput;

	public AllyRecruitmentMenu(ConsoleInput consoleInput) {
		this.consoleInput =
				Objects.requireNonNull(
						consoleInput,
						"Console input cannot be null.");
	}

	public void confirm(PlayableCharacter ally) {
		Objects.requireNonNull(
				ally,
				"Recruitment ally cannot be null.");

		PlayableClassDefinition classDefinition =
				ally.getPlayableClassDefinition();

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();

			System.out.println("[NOVA COMPANHIA]");
			System.out.println();

			System.out.println(
					ally.getName()
							+ " — ["
							+ classDefinition.getSymbol()
							+ "] "
							+ classDefinition.getName());

			System.out.println();
			System.out.println("[1] Aceitar sua companhia");

			if (feedbackMessage != null) {
				System.out.println();
				System.out.println(feedbackMessage);
			}

			System.out.println();
			System.out.print("> ");

			if (consoleInput
					.readTrimmedLine()
					.equals("1")) {
				return;
			}

			feedbackMessage = "Opção inválida.";
		}
	}
}
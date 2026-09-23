package rpg.ui;

import java.util.Objects;

// Requests confirmation before the player exits without saving.
public final class ExitConfirmationMenu {

	private final ConsoleInput consoleInput;

	public ExitConfirmationMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public boolean confirm() {
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println();
			ConsoleUI.displayHeading("Encerrar Jogo");

			System.out.println();
			System.out.println("Tem certeza de que deseja sair?");
			System.out.println("Todo o progresso realizado após o último salvamento será perdido.");
			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}
			System.out.println();
			System.out.println("[1] Encerrar sem salvar");
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}
}
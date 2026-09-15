package rpg.ui;

import java.util.Objects;
import java.util.Scanner;

// Requests confirmation before the player exits without saving.
public final class ExitConfirmationMenu {

	private final Scanner scanner;

	public ExitConfirmationMenu(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public boolean confirm() {
		while (true) {
			System.out.println();
			System.out.println("[ENCERRAR JOGO]");

			System.out.println();
			System.out.println("Tem certeza de que deseja sair?\nTodo o progresso realizado após o último salvamento será perdido.");

			System.out.println();
			System.out.println("[1] Encerrar sem salvar");
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			switch (choice) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}
}
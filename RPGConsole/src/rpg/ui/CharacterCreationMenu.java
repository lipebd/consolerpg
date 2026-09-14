package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import rpg.characters.CharacterNameRules;
import rpg.characters.CharacterNameSelector;
import rpg.classes.PlayableClassDefinition;

// Controls protagonist naming and character-creation confirmation.
public final class CharacterCreationMenu {

	private final CharacterNameSelector characterNameSelector;
	private final Scanner scanner;

	public CharacterCreationMenu(Scanner scanner, CharacterNameSelector characterNameSelector) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		this.characterNameSelector = Objects
				.requireNonNull(characterNameSelector, "Character name selector cannot be null.");
	}

	public Optional<String> open(PlayableClassDefinition classDefinition) {
		Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

		String proposedName = null;

		while (true) {
			if (proposedName == null) {
				Optional<String> selectedName = selectName(null);

				if (selectedName.isEmpty()) {
					return Optional.empty();
				}

				proposedName = selectedName.get();
			}

			displayConfirmation(proposedName, classDefinition);

			String choice = scanner.nextLine().trim();

			switch (choice) {
			case "1" -> {
				return Optional.of(proposedName);
			}

			case "2" -> {
				Optional<String> selectedName = selectName(proposedName);

				if (selectedName.isPresent()) {
					proposedName = selectedName.get();
				}
			}

			case "0" -> {
				return Optional.empty();
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private Optional<String> selectName(String currentName) {
		while (true) {
			System.out.println();
			System.out.println("[NOVO JOGO]");

			System.out.println();
			System.out.println("Como deseja escolher seu nome?");

			System.out.println();
			System.out.println("[1] Digitar nome");
			System.out.println("[2] Decida por mim");

			System.out.println();
			System.out.println(currentName == null ? "[0] Cancelar criação" : "[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			switch (choice) {
			case "1" -> {
		        Optional<String> manualName = readManualName();

		        if (manualName.isPresent()) {
		                return manualName;
		        }
		}

			case "2" -> {
				List<String> excludedNames = currentName == null ? List.of() : List.of(currentName);

				return Optional.of(characterNameSelector.selectExcluding(excludedNames));
			}

			case "0" -> {
				return Optional.empty();
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private Optional<String> readManualName() {
		while (true) {
			System.out.println();
			System.out.println("[NOME]");

			System.out.println();
			System.out.println(
					"Máximo de " + CharacterNameRules.getMaximumLength()
							+ " caracteres.\nUse letras, espaços, hífens e apóstrofos."
			);

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String enteredName = scanner.nextLine();

			if (enteredName.trim().equals("0")) {
				return Optional.empty();
			}

			try {
				return Optional.of(CharacterNameRules.requireValid(enteredName));
			} catch (IllegalArgumentException exception) {
				System.out.println("Nome inválido.");
			}
		}
	}

	private void displayConfirmation(String proposedName, PlayableClassDefinition classDefinition) {
		System.out.println();
		System.out.println("[CRIAÇÃO DE PERSONAGEM]");

		System.out.println();
		System.out.println("Nome: " + proposedName);
		System.out.println("Classe: " + classDefinition.getName());
		System.out.println(PlayableClassDetailFormatter.formatSpecializations(classDefinition));

		System.out.println();
		System.out.println("Descrição:");
		System.out.println(classDefinition.getDescription());

		System.out.println();
		System.out.println("[1] Confirmar");
		System.out.println("[2] Alterar nome");
		System.out.println("[0] Cancelar");
		System.out.print("> ");
	}
}
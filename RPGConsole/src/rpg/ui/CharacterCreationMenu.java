package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.CharacterNameRules;
import rpg.characters.CharacterNameSelector;
import rpg.classes.PlayableClassDefinition;

// Controls protagonist naming and character-creation confirmation.
public final class CharacterCreationMenu {

	private final CharacterNameSelector characterNameSelector;
	private final ConsoleInput consoleInput;

	public CharacterCreationMenu(ConsoleInput consoleInput, CharacterNameSelector characterNameSelector) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		this.characterNameSelector = Objects
				.requireNonNull(characterNameSelector, "Character name selector cannot be null.");
	}

	public Optional<String> open(PlayableClassDefinition classDefinition) {
		Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

		String proposedName = null;
		String feedbackMessage = null;
		while (true) {
			if (proposedName == null) {
				Optional<String> selectedName = selectName(null);

				if (selectedName.isEmpty()) {
					return Optional.empty();
				}

				proposedName = selectedName.get();
			}

			displayConfirmation(
			        proposedName,
			        classDefinition,
			        feedbackMessage);

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "1" -> {
				return Optional.of(proposedName);
			}

			case "2" -> {
			    feedbackMessage = null;

			    Optional<String> selectedName =
			            selectName(proposedName);

			    if (selectedName.isPresent()) {
			        proposedName = selectedName.get();
			    }
			}

			case "0" -> {
				return Optional.empty();
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private Optional<String> selectName(String currentName) {
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[RPGCONSOLE - NOVA CAMPANHA]");

			System.out.println();
			System.out.println("Como deseja escolher seu nome?");

			System.out.println();
			System.out.println("[1] Digitar nome");
			System.out.println("[2] Decida por mim");
			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println(
			        currentName == null
			                ? "[0] Cancelar criação"
			                : "[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "1" -> {
			    feedbackMessage = null;

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

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private Optional<String> readManualName() {
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[RPGCONSOLE - NOVA CAMPANHA]");

			System.out.println();
			System.out.println("Digite seu nome.");
			System.out.println();
			ConsoleUI.displayWrappedText(
			        "Máximo de "
			                + CharacterNameRules.getMaximumLength()
			                + " caracteres.\nUse letras, espaços, hífens e apóstrofos."
			);

			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String enteredName = consoleInput.readLine();

			if (enteredName.trim().equals("0")) {
				return Optional.empty();
			}

			try {
				return Optional.of(CharacterNameRules.requireValid(enteredName));
			} catch (IllegalArgumentException exception) {
				feedbackMessage = "Nome inválido.";
			}
		}
	}

	private void displayConfirmation(
	        String proposedName,
	        PlayableClassDefinition classDefinition,
	        String feedbackMessage
	) {
		ConsoleUI.clearScreen();
		System.out.println("[RPGCONSOLE - NOVA CAMPANHA]");

		System.out.println();
		System.out.println("Nome: " + proposedName);
		System.out.println();
		ConsoleUI.displayWrappedText(
		        PlayableClassDetailFormatter.formatPresentation(classDefinition));

		System.out.println();
		System.out.println("[1] Confirmar");
		System.out.println("[2] Alterar nome");
		if (feedbackMessage != null) {
		    System.out.println();
		    ConsoleUI.displayWrappedText(feedbackMessage);
		}

		System.out.println();
		System.out.println("[0] Cancelar");
		System.out.print("> ");
	}
}
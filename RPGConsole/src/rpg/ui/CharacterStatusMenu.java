package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.skills.SkillDefinition;

// Displays the detailed status of one playable character.
public final class CharacterStatusMenu {

	private final Party party;
	private final EquipmentMenu equipmentMenu;
	private final ConsoleInput consoleInput;

	public CharacterStatusMenu(Party party, ConsoleInput consoleInput, EquipmentMenu equipmentMenu) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		this.equipmentMenu = Objects.requireNonNull(equipmentMenu, "Equipment menu cannot be null.");
	}

	public void open(PlayableCharacter character) {
		Objects.requireNonNull(character, "Playable character cannot be null.");
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			displayCharacterStatus(character);

			System.out.println();
			System.out.println("[1] Habilidades");
			System.out.println("[2] Equipamentos");
			if (feedbackMessage != null) {
				ConsoleUI.displayWrappedText(feedbackMessage);
			} else {System.out.println();}
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readLine();

			switch (choice) {
			case "0" -> {
				return;
			}

			case "1" -> {
			    feedbackMessage = null;
			    displaySkills(character);
			}

			case "2" -> {
			    feedbackMessage = null;
			    equipmentMenu.openForCharacter(character);
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void displayCharacterStatus(PlayableCharacter character) {
		String nameSituation = "Nome: " + character.getName() + (character.isAlive() ? " | DERROTADO" : "");
		
		ConsoleUI.displayHeading("Status");
		System.out.println();
		System.out.println(nameSituation);

		System.out.println("Classe: " + character.getPlayableClassDefinition().getSymbol() + " " + character.getPlayableClassDefinition().getName());

		System.out.println("Nível: " + character.getLevel());

		System.out.println("EXP: " + formatExperience(character));

		System.out.println(
				"\nHP: " + character.getCurrentHp() + "/" + character.getMaxHp() + " | MP: " + character.getCurrentMp()
						+ "/" + character.getMaxMp() + " | SP: " + character.getCurrentSp() + "/" + character.getMaxSp()
		);

		System.out.println(
				"\nATK: " + character.getAttack() + " | DEF: " + character.getDefense() + " | VEL: "
						+ character.getSpeed()
		);

		System.out.println(
				"MAG: " + character.getMagic() + " | RES: " + character.getResistance() + " | LCK: "
						+ character.getLuck()
		);
	}

	private void displaySkills(PlayableCharacter character) {
		List<Optional<SkillDefinition>> skills = character.getSkillSlots();

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(
			        character.getName(),
			        "Habilidades");
			System.out.println();

			for (int index = 0; index < skills.size(); index++) {
			    Optional<SkillDefinition> skill = skills.get(index);

			    if (skill.isPresent()) {
			        SkillDefinition definition = skill.get();
			        System.out.println(
			                "[" + (index + 1) + "] " + definition.getName()
			                        + " [Nv. " + definition.getLevelRequirement() + "]"
			        );
			    } else {
			        System.out.println("    --");
			    }
			}

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readLine();

			if (choice.equals("0")) {
				return;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;
				if (selectedIndex < 0 || selectedIndex >= skills.size() || skills.get(selectedIndex).isEmpty()) {
				    feedbackMessage = "Habilidade inválida.";
				    continue;
				}

				feedbackMessage = null;
				displaySkillDetails(skills.get(selectedIndex).orElseThrow());
			} catch (NumberFormatException exception) {
				feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void displaySkillDetails(SkillDefinition skill) {
		List<String> detailLines = ConsoleUI.wrapText(SkillDetailFormatter.formatDetails(skill));
		final int contentLinesPerPage = 12;
		int pageCount = Math.max(1, (detailLines.size() + contentLinesPerPage - 1) / contentLinesPerPage);
		int page = 0;
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Habilidade");
			System.out.println();

			int firstLine = page * contentLinesPerPage;

			for (int offset = 0; offset < contentLinesPerPage; offset++) {
				int lineIndex = firstLine + offset;
				System.out.println(lineIndex < detailLines.size() ? detailLines.get(lineIndex) : "");
			}

			// Linha 15: sempre reservada para feedback.
			System.out.println(feedbackMessage == null ? "" : feedbackMessage);

			StringBuilder options = new StringBuilder("[0] Voltar");

			if (page > 0) {
				options.append("  [A] Anterior");
			}

			if (page + 1 < pageCount) {
				options.append("  [P] Próxima");
			}

			System.out.println(options);
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine().toUpperCase(java.util.Locale.ROOT);

			switch (choice) {
			case "0" -> {
				return;
			}
			case "A" -> {
				if (page > 0) {
					page--;
					feedbackMessage = null;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}
			case "P" -> {
				if (page + 1 < pageCount) {
					page++;
					feedbackMessage = null;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}
			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private String formatExperience(PlayableCharacter character) {
		int temporaryModifierPercent = party.getRewardModifierState().getExperienceModifierPercent();

		int equipmentModifierPercent = character.getEquipment().getExperienceModifierPercent();

		int totalModifierPercent = Math.addExact(temporaryModifierPercent, equipmentModifierPercent);

		String experienceProgress = character.isMaximumLevel()
				? "Nível máximo"
				: character.getExperience() + "/" + character.getExperienceRequiredForNextLevel();

		return experienceProgress + " | M.EXP: " + RewardModifierFormatter.formatMultiplier(totalModifierPercent);
	}

}

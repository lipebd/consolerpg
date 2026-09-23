package rpg.ui;

import java.util.List;
import java.util.Objects;

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
			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}
			System.out.println();
			System.out.println("[1] Habilidades");
			System.out.println("[2] Equipamentos");

			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
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
		System.out.println("\n[" + character.getName().toUpperCase() + " - STATUS]");
		System.out.println();
		if (!character.isAlive()) {
			System.out.println("Estado: DERROTADO");
		}

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
		List<SkillDefinition> skills = character.getUnlockedSkillDefinitions();

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[" + character.getName().toUpperCase() + " - HABILIDADES]");

			System.out.println();

			if (skills.isEmpty()) {
				System.out.println("Nenhuma habilidade aprendida.");
			} else {
				for (int index = 0; index < skills.size(); index++) {
					SkillDefinition skill = skills.get(index);

					System.out.println(
							"[" + (index + 1) + "] " + skill.getName() + " [Nv. " + skill.getLevelRequirement() + "]"
					);
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

			if (skills.isEmpty()) {
				feedbackMessage = "Opção inválida.";
				continue;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= skills.size()) {
					feedbackMessage = "Habilidade inválida.";
					continue;
				}

				feedbackMessage = null;
				displaySkillDetails(skills.get(selectedIndex), character);
			} catch (NumberFormatException exception) {
				feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void displaySkillDetails(SkillDefinition skill, PlayableCharacter character) {
		System.out.println("[" + character.getName().toUpperCase() + " - HABILIDADES]");
		System.out.println();
		String feedbackMessage = null;

	    while (true) {
	        ConsoleUI.clearScreen();
	        System.out.println(
	                SkillDetailFormatter.formatDetails(skill));

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

	        feedbackMessage = "Opção inválida.";
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

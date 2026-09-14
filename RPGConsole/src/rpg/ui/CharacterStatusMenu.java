package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.skills.SkillDefinition;

// Displays the detailed status of one playable character.
public final class CharacterStatusMenu {

	private final Party party;
	private final EquipmentMenu equipmentMenu;
	private final Scanner scanner;

	public CharacterStatusMenu(Party party, Scanner scanner, EquipmentMenu equipmentMenu) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		this.equipmentMenu = Objects.requireNonNull(equipmentMenu, "Equipment menu cannot be null.");
	}

	public void open(PlayableCharacter character) {
		Objects.requireNonNull(character, "Playable character cannot be null.");

		while (true) {
			displayCharacterStatus(character);

			System.out.println("\n[0] Voltar | [1] Habilidades | [2] Equipamentos");

			System.out.print("> ");

			String choice = scanner.nextLine();

			switch (choice) {
			case "0" -> {
				return;
			}

			case "1" -> displaySkills(character);

			case "2" -> equipmentMenu.openForCharacter(character);

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private void displayCharacterStatus(PlayableCharacter character) {
		System.out.println("\n[" + character.getName().toUpperCase() + "]");
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

		while (true) {
			System.out.println("\n[HABILIDADES DE " + character.getName().toUpperCase() + "]\n");

			if (skills.isEmpty()) {
				System.out.println("Nenhuma habilidade aprendida.");

				waitForBack();

				return;
			}

			for (int index = 0; index < skills.size(); index++) {
				SkillDefinition skill = skills.get(index);

				System.out.println(
						"[" + (index + 1) + "] " + skill.getName() + " [Nv. " + skill.getLevelRequirement() + "]"
				);
			}

			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= skills.size()) {

					System.out.println("Habilidade inválida.");

					continue;
				}

				displaySkillDetails(skills.get(selectedIndex));
			} catch (NumberFormatException exception) {
				System.out.println("Opção inválida.");
			}
		}
	}

	private void displaySkillDetails(SkillDefinition skill) {
		System.out.println("\n" + SkillDetailFormatter.formatDetails(skill));

		waitForBack();
	}

	private void waitForBack() {
		while (true) {
			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return;
			}

			System.out.println("Opção inválida.");
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

package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import rpg.characters.PlayableCharacter;
import rpg.skills.SkillDefinition;

// Displays the detailed status of one playable character.
public final class CharacterStatusMenu {

	private final Scanner scanner;

	public CharacterStatusMenu(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public void open(PlayableCharacter character) {
		Objects.requireNonNull(character, "Playable character cannot be null.");

		while (true) {
			displayCharacterStatus(character);

			System.out.println("\n[0] Voltar | [1] Habilidades");

			System.out.print("> ");

			String choice = scanner.nextLine();

			switch (choice) {
			case "0" -> {
				return;
			}

			case "1" -> displaySkills(character);

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private void displayCharacterStatus(PlayableCharacter character) {
		System.out.println("\n[" + character.getName().toUpperCase() + "]");

		if (!character.isAlive()) {
			System.out.println("Estado: DERROTADO");
		}

		System.out.println("Classe: " + character.getPlayableClassDefinition().getName());

		System.out.println("Nível: " + character.getLevel());

		System.out.println("EXP: " + character.getExperience() + "/" + character.getExperienceRequiredForNextLevel());

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
						"[" + (index + 1) + "] " + skill.getName() + " [Nv. " + skill.getAcquisitionLevel() + "]"
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
}
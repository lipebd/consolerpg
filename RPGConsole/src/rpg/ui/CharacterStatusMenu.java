package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import rpg.characters.PlayableCharacter;
import rpg.skills.Skill;

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

		System.out.println("Nível: " + character.getLevel());

		System.out.println("EXP: " + character.getExperience() + "/" + character.getExperienceRequiredForNextLevel());

		System.out.println("\nHP: " + character.getCurrentHp() + "/" + character.getMaxHp() + " | MP: "
				+ character.getCurrentMp() + "/" + character.getMaxMp() + " | SP: " + character.getCurrentSp() + "/"
				+ character.getMaxSp());

		System.out.println("\nATK: " + character.getAttack() + " | DEF: " + character.getDefense() + " | VEL: "
				+ character.getSpeed());

		System.out.println("MAG: " + character.getMagic() + " | RES: " + character.getResistance() + " | LCK: "
				+ character.getLuck());
	}

	private void displaySkills(PlayableCharacter character) {
		List<Skill> skills = character.getSkills();

		System.out.println("\n[HABILIDADES DE " + character.getName().toUpperCase() + "]");

		if (skills.isEmpty()) {
			System.out.println("Nenhuma habilidade aprendida.");
		} else {
			for (Skill skill : skills) {
				System.out.println("\n- " + skill.getName() + " | Nv. " + skill.getRequiredLevel());

				System.out.println("  " + skill.getDescription());
			}
		}

		waitForContinue();
	}

	private void waitForContinue() {
		System.out.println("\nPressione ENTER para voltar...");

		System.out.print("> ");
		scanner.nextLine();
	}
}
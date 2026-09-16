package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.SkillScroll;
import rpg.items.SkillScrollService;
import rpg.skills.SkillDefinition;
import rpg.skills.SkillLearningResult;

// Controls skill-scroll learning and scroll-skill replacement.
public final class SkillScrollUseMenu {

	private final Party party;
	private final ConsoleInput consoleInput;
	private final SkillScrollService skillScrollService;

	public SkillScrollUseMenu(Party party, ConsoleInput consoleInput) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		this.skillScrollService = new SkillScrollService();
	}

	public boolean use(SkillScroll skillScroll) {
		Objects.requireNonNull(skillScroll, "Skill scroll cannot be null.");

		Optional<PlayableCharacter> compatibleCharacter = findCompatibleCharacter(skillScroll);

		if (compatibleCharacter.isEmpty()) {
			System.out.println("Nenhum membro do grupo pode aprender esta habilidade.");
			ConsoleUI.pause(1000, consoleInput);
			return false;
		}

		PlayableCharacter character = compatibleCharacter.get();

		SkillLearningResult result = skillScrollService.use(party, character, skillScroll);

		return handleLearningResult(character, skillScroll, result);
	}

	private boolean
			handleLearningResult(PlayableCharacter character, SkillScroll skillScroll, SkillLearningResult result) {
		SkillDefinition skillDefinition = skillScroll.getSkillDefinition();

		return switch (result) {
		case LEARNED -> {
			System.out.println(character.getName() + " aprendeu " + skillDefinition.getName() + "!");
			ConsoleUI.pause(1000, consoleInput);
			yield true;
		}

		case ALREADY_LEARNED -> {
			System.out.println(character.getName() + " já sabe usar " + skillDefinition.getName() + ".");
			ConsoleUI.pause(1000, consoleInput);
			yield false;
		}

		case INCOMPATIBLE_CLASS -> {
			System.out.println(character.getName() + " não pertence à classe exigida pelo pergaminho.");
			ConsoleUI.pause(1000, consoleInput);
			yield false;
		}

		case LEVEL_TOO_LOW -> {
			System.out.println(
					character.getName() + " precisa alcançar o nível " + skillDefinition.getLevelRequirement()
							+ " para aprender esta habilidade."
			);
			ConsoleUI.pause(1000, consoleInput);
			yield false;
		}

		case SCROLL_LIMIT_REACHED -> executeReplacement(character, skillScroll);

		case REPLACED -> throw new IllegalStateException("Normal skill-scroll use cannot directly return REPLACED.");
		};
	}

	private boolean executeReplacement(PlayableCharacter character, SkillScroll skillScroll) {
		Optional<SkillDefinition> selectedSkill = selectReplacement(character, skillScroll);

		if (selectedSkill.isEmpty()) {
			return false;
		}

		SkillDefinition replacedSkill = selectedSkill.get();

		SkillLearningResult result = skillScrollService.replace(party, character, skillScroll, replacedSkill);

		if (result != SkillLearningResult.REPLACED) {
			throw new IllegalStateException("Validated scroll-skill replacement failed: " + result);
		}

		System.out.println(
				character.getName() + " esqueceu " + replacedSkill.getName() + " e aprendeu "
						+ skillScroll.getSkillDefinition().getName() + "!"
		);
		ConsoleUI.pause(1000, consoleInput);
		return true;
	}

	private Optional<SkillDefinition> selectReplacement(PlayableCharacter character, SkillScroll skillScroll) {
		List<SkillDefinition> learnedScrollSkills = character.getLearnedScrollSkillDefinitions();

		while (true) {
			displayReplacementMenu(character, skillScroll, learnedScrollSkills);

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= learnedScrollSkills.size()) {
					System.out.println("Habilidade inválida.");

					continue;
				}

				SkillDefinition selectedSkill = learnedScrollSkills.get(selectedIndex);

				if (confirmReplacement(selectedSkill, skillScroll)) {
					return Optional.of(selectedSkill);
				}
			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
			}
		}
	}

	private void displayReplacementMenu(
			PlayableCharacter character,
			SkillScroll skillScroll,
			List<SkillDefinition> learnedScrollSkills
	) {
		System.out.println();
		System.out.println("[LIMITE DE HABILIDADES]");

		System.out.println();
		System.out.println(character.getName() + " já aprendeu o máximo de duas habilidades de pergaminho.");
		ConsoleUI.pause(1000, consoleInput);
		System.out.println(
				"Escolha uma habilidade para substituir por " + skillScroll.getSkillDefinition().getName() + ":"
		);
		System.out.println();

		for (int index = 0; index < learnedScrollSkills.size(); index++) {
			System.out.println("[" + (index + 1) + "] " + learnedScrollSkills.get(index).getName());
		}

		System.out.println();
		System.out.println("[0] Cancelar");
		System.out.print("> ");
	}

	private boolean confirmReplacement(SkillDefinition replacedSkill, SkillScroll skillScroll) {
		while (true) {
			System.out.println();
			System.out.println("[CONFIRMAR SUBSTITUIÇÃO]");

			System.out.println();
			System.out.println("Esquecer: " + replacedSkill.getName());
			System.out.println("Aprender: " + skillScroll.getSkillDefinition().getName());

			System.out.println();
			System.out.println("Essa ação é irreversível.");

			System.out.println();
			System.out.println("[1] Confirmar");
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("1")) {
				return true;
			}

			if (choice.equals("0")) {
				return false;
			}

			System.out.println("Opção inválida.");
		}
	}

	public Optional<PlayableCharacter> findCompatibleCharacter(SkillScroll skillScroll) {
		List<PlayableCharacter> compatibleCharacters = party.getMembers().stream().filter(
				character -> character.getPlayableClassDefinition().getId().equals(skillScroll.getCompatibleClassId())
		).toList();

		if (compatibleCharacters.size() > 1) {
			throw new IllegalStateException(
					"Party contains multiple characters compatible with skill scroll: " + skillScroll.getId()
			);
		}

		return compatibleCharacters.stream().findFirst();
	}
}
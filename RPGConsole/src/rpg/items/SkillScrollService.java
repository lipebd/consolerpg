package rpg.items;

import java.util.Objects;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.skills.SkillDefinition;
import rpg.skills.SkillLearningResult;

// Coordinates skill-scroll learning and inventory consumption.
public final class SkillScrollService {

	public SkillLearningResult use(Party party, PlayableCharacter character, SkillScroll skillScroll) {
		validateUse(party, character, skillScroll);

		SkillLearningResult result = character.learnSkill(skillScroll.getSkillDefinition());

		if (result == SkillLearningResult.LEARNED) {
			consumeSkillScroll(party, skillScroll);
		}

		return result;
	}

	public SkillLearningResult replace(
			Party party,
			PlayableCharacter character,
			SkillScroll skillScroll,
			SkillDefinition replacedSkillDefinition
	) {
		validateUse(party, character, skillScroll);

		Objects.requireNonNull(replacedSkillDefinition, "Replaced skill definition cannot be null.");

		SkillLearningResult result = character
				.replaceLearnedScrollSkill(replacedSkillDefinition, skillScroll.getSkillDefinition());

		if (result == SkillLearningResult.REPLACED) {
			consumeSkillScroll(party, skillScroll);
		}

		return result;
	}

	private void validateUse(Party party, PlayableCharacter character, SkillScroll skillScroll) {
		Objects.requireNonNull(party, "Party cannot be null.");

		Objects.requireNonNull(character, "Playable character cannot be null.");

		Objects.requireNonNull(skillScroll, "Skill scroll cannot be null.");

		if (!party.getMembers().contains(character)) {
			throw new IllegalArgumentException("Character does not belong to the party: " + character.getName());
		}

		if (party.getInventory().getQuantity(skillScroll) < 1) {
			throw new IllegalArgumentException("Party does not own the skill scroll: " + skillScroll.getId());
		}
	}

	private void consumeSkillScroll(Party party, SkillScroll skillScroll) {
		if (!party.getInventory().removeItem(skillScroll, 1)) {
			throw new IllegalStateException(
					"Successfully used skill scroll could not be removed from inventory: " + skillScroll.getId()
			);
		}
	}
}
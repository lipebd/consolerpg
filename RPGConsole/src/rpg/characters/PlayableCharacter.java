package rpg.characters;

import java.util.List;
import java.util.Objects;

import rpg.skills.SkillDefinition;

// Represents a character controlled by the player.
public class PlayableCharacter extends GameCharacter {

	private static final int BASE_EXPERIENCE_REQUIREMENT = 50;

	private int experience;
	private int level;
	private final List<SkillDefinition> classSkillDefinitions;

	public PlayableCharacter(String name, CharacterStats stats, List<SkillDefinition> classSkillDefinitions) {
		super(name, stats);

		this.level = 1;
		this.classSkillDefinitions = List
				.copyOf(Objects.requireNonNull(classSkillDefinitions, "Class skill definitions cannot be null."));
		this.experience = 0;
	}

	public List<SkillDefinition> getUnlockedSkillDefinitions() {
		return classSkillDefinitions.stream().filter(skillDefinition -> level >= skillDefinition.getAcquisitionLevel())
				.toList();
	}

	public int getLevel() {
		return level;
	}

	public int gainExperience(int amount) {
		if (amount <= 0) {
			return 0;
		}

		experience = experience + amount;

		int levelsGained = 0;

		while (experience >= getExperienceRequiredForNextLevel()) {
			experience = experience - getExperienceRequiredForNextLevel();

			level++;
			levelsGained++;
		}

		return levelsGained;
	}

	public int getExperienceRequiredForNextLevel() {
		return BASE_EXPERIENCE_REQUIREMENT * level;
	}

	public int getExperience() {
		return experience;
	}

}

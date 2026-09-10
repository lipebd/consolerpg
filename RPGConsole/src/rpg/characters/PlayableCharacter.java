package rpg.characters;

import java.util.List;
import java.util.Objects;

import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassDefinition;
import rpg.skills.SkillDefinition;

// Represents a character controlled by the player.
public class PlayableCharacter extends GameCharacter {

	private static final int BASE_EXPERIENCE_REQUIREMENT = 50;

	private final PlayableClassDefinition classDefinition;
	private final CharacterGrowthCalculator growthCalculator;
	private final List<SkillDefinition> classSkillDefinitions;

	private int experience;
	private int level;

	public PlayableCharacter(
			String name,
			PlayableClassDefinition classDefinition,
			List<SkillDefinition> classSkillDefinitions,
			CharacterGrowthCalculator growthCalculator
	) {
		super(
				name,
				Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.").getBaseStats()
		);

		this.classDefinition = classDefinition;

		this.classSkillDefinitions = List
				.copyOf(Objects.requireNonNull(classSkillDefinitions, "Class skill definitions cannot be null."));

		this.growthCalculator = Objects.requireNonNull(growthCalculator, "Character growth calculator cannot be null.");

		this.level = 1;
		this.experience = 0;
	}

	public List<SkillDefinition> getUnlockedSkillDefinitions() {
		return classSkillDefinitions.stream().filter(skillDefinition -> level >= skillDefinition.getAcquisitionLevel())
				.toList();
	}

	public int gainExperience(int amount) {
		if (amount <= 0) {
			return 0;
		}

		experience = Math.addExact(experience, amount);

		int levelsGained = 0;

		while (experience >= getExperienceRequiredForNextLevel()) {
			experience -= getExperienceRequiredForNextLevel();

			level = Math.incrementExact(level);
			levelsGained++;
		}

		if (levelsGained > 0) {
			CharacterStats naturalStats = growthCalculator.calculateNaturalStats(classDefinition, level);

			replaceNaturalStats(naturalStats);
		}

		return levelsGained;
	}

	public int getExperienceRequiredForNextLevel() {
		return Math.multiplyExact(BASE_EXPERIENCE_REQUIREMENT, level);
	}

	public PlayableClassDefinition getPlayableClassDefinition() {
		return classDefinition;
	}

	public int getExperience() {
		return experience;
	}

	public int getLevel() {
		return level;
	}
}
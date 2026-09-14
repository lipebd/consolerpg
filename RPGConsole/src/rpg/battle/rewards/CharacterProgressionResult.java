package rpg.battle.rewards;

import java.util.List;
import java.util.Objects;

import rpg.characters.CharacterStats;
import rpg.characters.PlayableCharacter;
import rpg.skills.SkillDefinition;

// Stores the experience and level progression of one character.
public final class CharacterProgressionResult {

	private final PlayableCharacter character;
	private final int experienceReceived;
	private final int previousLevel;
	private final int currentLevel;
	private final boolean resourcesRestored;
	private final CharacterStats previousNaturalStats;
	private final CharacterStats currentNaturalStats;
	private final List<SkillDefinition> newlyUnlockedSkillDefinitions;

	public CharacterProgressionResult(
			PlayableCharacter character,
			int experienceReceived,
			int previousLevel,
			int currentLevel,
			boolean resourcesRestored,
			CharacterStats previousNaturalStats,
			CharacterStats currentNaturalStats,
			List<SkillDefinition> newlyUnlockedSkillDefinitions
	) {
		if (experienceReceived < 0) {
			throw new IllegalArgumentException("Received experience cannot be negative.");
		}

		if (previousLevel < 1 || currentLevel < 1) {
			throw new IllegalArgumentException("Character levels must be positive.");
		}

		if (currentLevel < previousLevel) {
			throw new IllegalArgumentException("Current level cannot be lower than previous level.");
		}

		if (resourcesRestored && currentLevel == previousLevel) {
			throw new IllegalArgumentException("Resources cannot be restored without a level up.");
		}

		Objects.requireNonNull(newlyUnlockedSkillDefinitions, "Newly unlocked skill definitions cannot be null.");

		if (currentLevel == previousLevel && !newlyUnlockedSkillDefinitions.isEmpty()) {
			throw new IllegalArgumentException("Skills cannot be unlocked without a level up.");
		}

		this.character = Objects.requireNonNull(character, "Character cannot be null.");

		this.previousNaturalStats = Objects
				.requireNonNull(previousNaturalStats, "Previous natural stats cannot be null.");

		this.currentNaturalStats = Objects.requireNonNull(currentNaturalStats, "Current natural stats cannot be null.");

		this.experienceReceived = experienceReceived;
		this.previousLevel = previousLevel;
		this.currentLevel = currentLevel;
		this.resourcesRestored = resourcesRestored;

		this.newlyUnlockedSkillDefinitions = List.copyOf(newlyUnlockedSkillDefinitions);
	}

	public int getLevelsGained() {
		return currentLevel - previousLevel;
	}

	public PlayableCharacter getCharacter() {
		return character;
	}

	public int getExperienceReceived() {
		return experienceReceived;
	}

	public int getPreviousLevel() {
		return previousLevel;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public boolean wereResourcesRestored() {
		return resourcesRestored;
	}

	public CharacterStats getPreviousNaturalStats() {
		return previousNaturalStats;
	}

	public CharacterStats getCurrentNaturalStats() {
		return currentNaturalStats;
	}

	public List<SkillDefinition> getNewlyUnlockedSkillDefinitions() {

		return newlyUnlockedSkillDefinitions;
	}
}
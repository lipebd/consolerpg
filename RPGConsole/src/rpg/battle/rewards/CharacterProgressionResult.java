package rpg.battle.rewards;

import java.util.Objects;

import rpg.characters.PlayableCharacter;

// Stores the experience and level progression of one character.
public final class CharacterProgressionResult {

	private final PlayableCharacter character;
	private final int experienceReceived;
	private final int previousLevel;
	private final int currentLevel;

	public CharacterProgressionResult(PlayableCharacter character, int experienceReceived, int previousLevel,
			int currentLevel) {
		if (experienceReceived < 0) {
			throw new IllegalArgumentException("Received experience cannot be negative.");
		}

		if (currentLevel < previousLevel) {
			throw new IllegalArgumentException("Current level cannot be lower than previous level.");
		}

		this.character = Objects.requireNonNull(character, "Character cannot be null.");

		this.experienceReceived = experienceReceived;
		this.previousLevel = previousLevel;
		this.currentLevel = currentLevel;
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
}
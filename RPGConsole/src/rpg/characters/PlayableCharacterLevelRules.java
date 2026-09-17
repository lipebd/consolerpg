package rpg.characters;

// Defines the supported level range and experience requirements for playable characters.
public final class PlayableCharacterLevelRules {

	private static final int BASE_EXPERIENCE_REQUIREMENT = 50;

	public static final int MINIMUM_LEVEL = 1;
	public static final int MAXIMUM_LEVEL = 20;

	private PlayableCharacterLevelRules() {
	}

	public static void validateLevel(int level) {
		if (level < MINIMUM_LEVEL || level > MAXIMUM_LEVEL) {
			throw new IllegalArgumentException(
					"Playable character level must be between " + MINIMUM_LEVEL + " and " + MAXIMUM_LEVEL + ": " + level
			);
		}
	}

	public static boolean isMaximumLevel(int level) {
		validateLevel(level);

		return level == MAXIMUM_LEVEL;
	}

	public static int getExperienceRequiredForNextLevel(int level) {
		validateLevel(level);

		if (isMaximumLevel(level)) {
			throw new IllegalStateException("Maximum-level characters have no next-level experience requirement.");
		}

		return Math.multiplyExact(BASE_EXPERIENCE_REQUIREMENT, level);
	}
}
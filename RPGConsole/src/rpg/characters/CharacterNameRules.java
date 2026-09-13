package rpg.characters;

import java.text.Normalizer;
import java.util.Objects;

// Centralizes playable-character name normalization, validation and comparison.
public final class CharacterNameRules {

	private static final char SPACE = ' ';
	private static final int MAXIMUM_LENGTH = 16;

	private CharacterNameRules() {
	}

	public static String requireValid(String name) {
		Objects.requireNonNull(name, "Character name cannot be null.");

		String normalizedName = normalize(name);

		if (!isValidNormalizedName(normalizedName)) {
			throw new IllegalArgumentException("Invalid character name: " + normalizedName);
		}

		return normalizedName;
	}

	public static boolean isValid(String name) {
		if (name == null) {
			return false;
		}

		return isValidNormalizedName(normalize(name));
	}

	public static boolean areEquivalent(String firstName, String secondName) {
		if (firstName == null || secondName == null) {
			return false;
		}

		return normalize(firstName).equalsIgnoreCase(normalize(secondName));
	}

	public static int getMaximumLength() {
		return MAXIMUM_LENGTH;
	}

	private static String normalize(String name) {
		String unicodeNormalizedName = Normalizer.normalize(name, Normalizer.Form.NFC);

		StringBuilder normalizedName = new StringBuilder();
		boolean previousCharacterWasSpace = true;

		for (int index = 0; index < unicodeNormalizedName.length();) {
			int codePoint = unicodeNormalizedName.codePointAt(index);

			if (codePoint == SPACE) {
				if (!previousCharacterWasSpace) {
					normalizedName.append(SPACE);
					previousCharacterWasSpace = true;
				}
			} else {
				normalizedName.appendCodePoint(codePoint);
				previousCharacterWasSpace = false;
			}

			index += Character.charCount(codePoint);
		}

		int length = normalizedName.length();

		if (length > 0 && normalizedName.charAt(length - 1) == SPACE) {
			normalizedName.deleteCharAt(length - 1);
		}

		return normalizedName.toString();
	}

	private static boolean isValidNormalizedName(String name) {
		if (name.isEmpty()) {
			return false;
		}

		int[] codePoints = name.codePoints().toArray();

		if (codePoints.length > MAXIMUM_LENGTH) {
			return false;
		}

		if (!Character.isLetter(codePoints[0]) || !Character.isLetter(codePoints[codePoints.length - 1])) {
			return false;
		}

		for (int index = 0; index < codePoints.length; index++) {
			int codePoint = codePoints[index];

			if (Character.isLetter(codePoint)) {
				continue;
			}

			if (!isSeparator(codePoint)) {
				return false;
			}

			if (!Character.isLetter(codePoints[index - 1]) || !Character.isLetter(codePoints[index + 1])) {
				return false;
			}
		}

		return true;
	}

	private static boolean isSeparator(int codePoint) {
		return codePoint == SPACE || codePoint == '-' || codePoint == '\'';
	}
}
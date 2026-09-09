package rpg.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import rpg.characters.CharacterAttribute;
import rpg.characters.CharacterStats;

// Loads playable-class definitions from an external text file.
public final class PlayableClassLoader {

	private static final int EXPECTED_FIELD_COUNT = 15;

	public PlayableClassCatalog load(Path filePath) throws IOException {
		PlayableClassCatalog catalog = new PlayableClassCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			line = line.trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			PlayableClassDefinition classDefinition = parseLine(line);

			catalog.register(classDefinition);
		}

		return catalog;
	}

	private PlayableClassDefinition parseLine(String line) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Invalid playable-class field count. Expected " + EXPECTED_FIELD_COUNT + " fields: " + line
			);
		}

		String id = fields[0].trim();
		String name = fields[1].trim();

		int baseHp = parseInteger(fields[2], "base HP", id);

		int baseMp = parseInteger(fields[3], "base MP", id);

		int baseSp = parseInteger(fields[4], "base SP", id);

		int baseAttack = parseInteger(fields[5], "base ATK", id);

		int baseDefense = parseInteger(fields[6], "base DEF", id);

		int baseMagic = parseInteger(fields[7], "base MAG", id);

		int baseResistance = parseInteger(fields[8], "base RES", id);

		int baseSpeed = parseInteger(fields[9], "base VEL", id);

		int baseLuck = parseInteger(fields[10], "base LCK", id);

		CharacterAttribute primaryAttribute = parseAttribute(fields[11], "primary", id);

		CharacterAttribute secondaryAttribute = parseAttribute(fields[12], "secondary", id);

		PlayableClassWeight weight = parseWeight(fields[13], id);

		String description = fields[14].trim().replace("\\n", System.lineSeparator());

		CharacterStats baseStats = new CharacterStats(
				baseHp,
				baseMp,
				baseSp,
				baseAttack,
				baseDefense,
				baseMagic,
				baseResistance,
				baseSpeed,
				baseLuck
		);

		return new PlayableClassDefinition(
				id,
				name,
				baseStats,
				primaryAttribute,
				secondaryAttribute,
				weight,
				description
		);
	}

	private int parseInteger(String value, String fieldName, String classId) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Invalid playable-class " + fieldName + " for " + classId + ": " + value,
					exception
			);
		}
	}

	private CharacterAttribute parseAttribute(String value, String role, String classId) {
		try {
			return CharacterAttribute.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid playable-class " + role + " attribute for " + classId + ": " + value,
					exception
			);
		}
	}

	private PlayableClassWeight parseWeight(String value, String classId) {
		try {
			return PlayableClassWeight.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid playable-class weight for " + classId + ": " + value,
					exception
			);
		}
	}
}
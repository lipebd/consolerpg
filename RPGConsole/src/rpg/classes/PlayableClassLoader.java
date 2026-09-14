package rpg.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import rpg.characters.CharacterAttribute;
import rpg.characters.CharacterStats;
import rpg.items.equipment.ArmorWeight;

// Loads playable-class definitions from an external text file.
public final class PlayableClassLoader {

	private static final int EXPECTED_FIELD_COUNT = 16;

	public PlayableClassCatalog load(Path filePath) throws IOException {
		PlayableClassCatalog catalog = new PlayableClassCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {

			String line = lines.get(lineIndex).trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			int lineNumber = lineIndex + 1;

			try {
				PlayableClassDefinition classDefinition = parseLine(line);

				catalog.register(classDefinition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid playable-class definition at line " + lineNumber + " in " + filePath + ": "
								+ exception.getMessage(),
						exception
				);
			}
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
		String symbol = fields[2].trim();

		int baseHp = parseInteger(fields[3], "base HP", id);

		int baseMp = parseInteger(fields[4], "base MP", id);

		int baseSp = parseInteger(fields[5], "base SP", id);

		int baseAttack = parseInteger(fields[6], "base ATK", id);

		int baseDefense = parseInteger(fields[7], "base DEF", id);

		int baseMagic = parseInteger(fields[8], "base MAG", id);

		int baseResistance = parseInteger(fields[9], "base RES", id);

		int baseSpeed = parseInteger(fields[10], "base VEL", id);

		int baseLuck = parseInteger(fields[11], "base LCK", id);

		CharacterAttribute primaryAttribute = parseAttribute(fields[12], "primary", id);

		CharacterAttribute secondaryAttribute = parseAttribute(fields[13], "secondary", id);

		ArmorWeight armorWeight = parseArmorWeight(fields[14], id);

		String description = fields[15].trim().replace("\\n", System.lineSeparator());

		CharacterStats baseStats;

		try {
			baseStats = new CharacterStats(
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
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid stats for playable class '" + id + "': " + exception.getMessage(),
					exception
			);
		}

		return new PlayableClassDefinition(
				id,
				name,
				symbol,
				baseStats,
				primaryAttribute,
				secondaryAttribute,
				armorWeight,
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

	private ArmorWeight parseArmorWeight(String value, String classId) {
		try {
			return ArmorWeight.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid playable-class armor weight for " + classId + ": " + value,
					exception
			);
		}
	}
}
package rpg.enemies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import rpg.characters.CharacterAttribute;
import rpg.characters.CharacterStats;
import rpg.characters.EnemyRank;

// Loads enemy definitions from an external text file.
public class EnemyLoader {

	public EnemyCatalog load(Path filePath) throws IOException {
		EnemyCatalog catalog = new EnemyCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {

			String line = lines.get(lineIndex).trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			int lineNumber = lineIndex + 1;

			try {
				EnemyDefinition enemyDefinition = parseLine(line);

				catalog.register(enemyDefinition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid enemy definition at line " + lineNumber + " in " + filePath + ": "
								+ exception.getMessage(),
						exception
				);
			}
		}

		return catalog;
	}

	private EnemyDefinition parseLine(String line) {
		String[] fields = line.split(";", -1);

		if (fields.length != 16) {
			throw new IllegalArgumentException(
					"Invalid enemy data. Expected 16 fields but found " + fields.length + ": " + line
			);
		}

		String id = fields[0].trim();
		String name = fields[1].trim();

		int maxHp = Integer.parseInt(fields[2].trim());
		int maxMp = Integer.parseInt(fields[3].trim());
		int maxSp = Integer.parseInt(fields[4].trim());
		int attack = Integer.parseInt(fields[5].trim());
		int defense = Integer.parseInt(fields[6].trim());
		int magic = Integer.parseInt(fields[7].trim());
		int resistance = Integer.parseInt(fields[8].trim());
		int speed = Integer.parseInt(fields[9].trim());
		int luck = Integer.parseInt(fields[10].trim());

		CharacterAttribute primaryAttribute = parseOptionalAttribute(fields[11], "primary", id);
		CharacterAttribute secondaryAttribute = parseOptionalAttribute(fields[12], "secondary", id);
		CharacterAttribute weaknessAttribute = parseOptionalAttribute(fields[13], "weakness", id);

		EnemyRank rank = EnemyRank.valueOf(fields[14].trim().toUpperCase(Locale.ROOT));
		String description = fields[15].trim().replace("\\n", System.lineSeparator());

		CharacterStats stats;

		try {
			stats = new CharacterStats(maxHp, maxMp, maxSp, attack, defense, magic, resistance, speed, luck);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid stats for enemy '" + id + "': " + exception.getMessage(),
					exception
			);
		}

		return new EnemyDefinition(
				id,
				name,
				stats,
				primaryAttribute,
				secondaryAttribute,
				weaknessAttribute,
				rank,
				List.of(),
				description
		);
	}

	private CharacterAttribute parseOptionalAttribute(String field, String position, String enemyId) {
		String value = field.trim();

		if (value.isEmpty()) {
			return null;
		}

		try {
			return CharacterAttribute.valueOf(value.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid " + position + " attribute for enemy '" + enemyId + "': " + value,
					exception
			);
		}
	}
}
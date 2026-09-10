package rpg.enemies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

		if (fields.length != 13) {
			throw new IllegalArgumentException("Dados inválidos: " + line);
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

		EnemyRank rank = EnemyRank.valueOf(fields[11].trim().toUpperCase());

		String description = fields[12].trim().replace("\\n", System.lineSeparator());

		CharacterStats stats;

		try {
			stats = new CharacterStats(maxHp, maxMp, maxSp, attack, defense, magic, resistance, speed, luck);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid stats for enemy '" + id + "': " + exception.getMessage(),
					exception
			);
		}

		return new EnemyDefinition(id, name, stats, rank, List.of(), description);
	}
}
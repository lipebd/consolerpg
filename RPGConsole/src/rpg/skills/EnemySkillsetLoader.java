package rpg.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import rpg.characters.CharacterStats;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDefinition;

// Loads enemy-to-skill links from the external skillset file.
public final class EnemySkillsetLoader {

	private static final int FIELD_COUNT = 3;

	public EnemySkillsetCatalog load(Path filePath, EnemyCatalog enemyCatalog, EnemySkillCatalog enemySkillCatalog)
			throws IOException {
		Objects.requireNonNull(filePath, "Enemy skillset file path cannot be null.");
		Objects.requireNonNull(enemyCatalog, "Enemy catalog cannot be null.");
		Objects.requireNonNull(enemySkillCatalog, "Enemy skill catalog cannot be null.");

		EnemySkillsetCatalog skillsetCatalog = new EnemySkillsetCatalog();
		Set<String> linkedSkillIds = new LinkedHashSet<>();

		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			try {
				EnemySkillsetEntry entry = parseEntry(line, enemyCatalog, enemySkillCatalog);

				skillsetCatalog.register(entry);
				linkedSkillIds.add(entry.skillDefinition().getId());
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid enemy skillset entry at line " + (index + 1) + ": " + line,
						exception
				);
			}
		}

		validateSkillCoverage(enemySkillCatalog, linkedSkillIds);

		return skillsetCatalog;
	}

	private EnemySkillsetEntry parseEntry(String line, EnemyCatalog enemyCatalog, EnemySkillCatalog enemySkillCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != FIELD_COUNT) {
			throw new IllegalArgumentException("Expected " + FIELD_COUNT + " fields.");
		}

		String enemyId = fields[0].trim();
		String skillId = fields[1].trim();
		int minimumLevel = parseMinimumLevel(fields[2], enemyId, skillId);

		EnemyDefinition enemyDefinition = enemyCatalog.getById(enemyId);
		EnemySkillDefinition skillDefinition = enemySkillCatalog.getById(skillId);

		validateResourceCapacity(enemyDefinition, skillDefinition, minimumLevel);

		return new EnemySkillsetEntry(enemyId, skillDefinition, minimumLevel);
	}

	private void validateResourceCapacity(
			EnemyDefinition enemyDefinition,
			EnemySkillDefinition skillDefinition,
			int minimumLevel
	) {
		CharacterStats statsAtMinimumLevel = enemyDefinition.calculateNaturalStats(minimumLevel);

		if (skillDefinition.getMpCost() > statsAtMinimumLevel.getMaxMp()) {
			throw new IllegalArgumentException(
					"Enemy does not have enough maximum MP at level " + minimumLevel + " to use linked skill: "
							+ enemyDefinition.getId() + " -> " + skillDefinition.getId()
			);
		}

		if (skillDefinition.getSpCost() > statsAtMinimumLevel.getMaxSp()) {
			throw new IllegalArgumentException(
					"Enemy does not have enough maximum SP at level " + minimumLevel + " to use linked skill: "
							+ enemyDefinition.getId() + " -> " + skillDefinition.getId()
			);
		}
	}

	private int parseMinimumLevel(String value, String enemyId, String skillId) {
		String trimmedValue = value.trim();

		if (trimmedValue.isEmpty()) {
			throw new IllegalArgumentException(
					"Missing minimum level for enemy skillset link: " + enemyId + " -> " + skillId
			);
		}

		try {
			return Integer.parseInt(trimmedValue);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Invalid minimum level for enemy skillset link: " + enemyId + " -> " + skillId,
					exception
			);
		}
	}

	private void validateSkillCoverage(EnemySkillCatalog enemySkillCatalog, Set<String> linkedSkillIds) {
		for (EnemySkillDefinition skillDefinition : enemySkillCatalog.getAll()) {
			if (!linkedSkillIds.contains(skillDefinition.getId())) {
				throw new IllegalArgumentException(
						"Enemy skill is not linked to any enemy: " + skillDefinition.getId()
				);
			}
		}
	}
}
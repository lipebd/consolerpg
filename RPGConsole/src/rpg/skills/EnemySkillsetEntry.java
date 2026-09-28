package rpg.skills;

import java.util.Objects;

// Links one enemy species to an enemy skill and its minimum level.
public record EnemySkillsetEntry(String enemyId, EnemySkillDefinition skillDefinition, int minimumLevel) {

	public EnemySkillsetEntry {
		if (enemyId == null || enemyId.isBlank()) {
			throw new IllegalArgumentException("Enemy skillset enemy ID cannot be empty.");
		}

		enemyId = enemyId.trim();

		Objects.requireNonNull(skillDefinition, "Enemy skillset skill definition cannot be null.");

		if (minimumLevel < 1) {
			throw new IllegalArgumentException(
					"Enemy skillset minimum level must be positive: " + enemyId + " -> " + skillDefinition.getId()
			);
		}
	}
}
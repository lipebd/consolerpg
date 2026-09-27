package rpg.enemies;

import java.util.Objects;

// Identifies an enemy species and the level of one pending battle instance.
public record EnemySpawn(EnemyDefinition definition, int level) {

	public EnemySpawn {
		Objects.requireNonNull(definition, "Enemy definition cannot be null.");

		if (level < 1) {
			throw new IllegalArgumentException("Enemy level must be at least one.");
		}
	}
}
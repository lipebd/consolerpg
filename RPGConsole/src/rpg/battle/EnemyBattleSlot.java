package rpg.battle;

import java.util.Objects;

import rpg.characters.Enemy;

// Represents one enemy position inside a battle.
public final class EnemyBattleSlot {

	private final Enemy enemy;
	private final String displayName;

	public EnemyBattleSlot(Enemy enemy, String displayName) {
		this.enemy = Objects.requireNonNull(enemy, "Enemy cannot be null.");

		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("Enemy display name cannot be empty.");
		}

		this.displayName = displayName;
	}

	public Enemy getEnemy() {
		return enemy;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isAlive() {
		return enemy.isAlive();
	}
}
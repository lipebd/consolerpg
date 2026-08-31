package rpg.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.characters.Enemy;

// Stores and identifies all enemies participating in one battle.
public final class EnemyBattleGroup {

	public static final int MAXIMUM_SIZE = 3;

	private final List<EnemyBattleSlot> slots;

	public EnemyBattleGroup(List<Enemy> enemies) {
		Objects.requireNonNull(enemies, "Enemy list cannot be null.");

		if (enemies.isEmpty()) {
			throw new IllegalArgumentException("Enemy group cannot be empty.");
		}

		if (enemies.size() > MAXIMUM_SIZE) {
			throw new IllegalArgumentException("Enemy group cannot contain more than " + MAXIMUM_SIZE + " enemies.");
		}

		this.slots = createSlots(enemies);
	}

	private List<EnemyBattleSlot> createSlots(List<Enemy> enemies) {
		Map<String, Integer> totalByName = countEnemiesByName(enemies);

		Map<String, Integer> currentNumberByName = new HashMap<>();

		List<EnemyBattleSlot> createdSlots = new ArrayList<>();

		for (Enemy enemy : enemies) {
			Objects.requireNonNull(enemy, "Enemy cannot be null.");

			String enemyName = enemy.getName();

			int totalWithSameName = totalByName.get(enemyName);

			String displayName = enemyName;

			if (totalWithSameName > 1) {
				int currentNumber = currentNumberByName.merge(enemyName, 1, Integer::sum);

				displayName = enemyName + " " + currentNumber;
			}

			createdSlots.add(new EnemyBattleSlot(enemy, displayName));
		}

		return List.copyOf(createdSlots);
	}

	private Map<String, Integer> countEnemiesByName(List<Enemy> enemies) {
		Map<String, Integer> totalByName = new HashMap<>();

		for (Enemy enemy : enemies) {
			Objects.requireNonNull(enemy, "Enemy cannot be null.");

			totalByName.merge(enemy.getName(), 1, Integer::sum);
		}

		return totalByName;
	}

	public List<EnemyBattleSlot> getSlots() {
		return slots;
	}

	public List<EnemyBattleSlot> getLivingSlots() {
		return slots.stream().filter(EnemyBattleSlot::isAlive).toList();
	}

	public List<Enemy> getEnemies() {
		return slots.stream().map(EnemyBattleSlot::getEnemy).toList();
	}

	public boolean isDefeated() {
		return slots.stream().noneMatch(EnemyBattleSlot::isAlive);
	}

	public int size() {
		return slots.size();
	}
}
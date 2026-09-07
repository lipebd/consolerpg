package rpg.world.scenarios;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import rpg.battle.EnemyBattleGroup;

// Stores the enemy configuration specific to a dungeon.
public final class DungeonSettings {

	private final List<String> bossEnemyIds;
	private final List<String> epicEnemyIds;

	public DungeonSettings(String bossEnemyId, String epicEnemyId) {
		this(bossEnemyId == null ? null : List.of(bossEnemyId), epicEnemyId == null ? null : List.of(epicEnemyId));
	}

	public DungeonSettings(List<String> bossEnemyIds, List<String> epicEnemyIds) {
		List<String> validatedBossEnemyIds = validateEnemyIds(bossEnemyIds, "Boss");

		List<String> validatedEpicEnemyIds = validateEnemyIds(epicEnemyIds, "Epic");

		if (!Collections.disjoint(validatedBossEnemyIds, validatedEpicEnemyIds)) {
			throw new IllegalArgumentException("Boss and epic enemy IDs must be different.");
		}

		this.bossEnemyIds = validatedBossEnemyIds;
		this.epicEnemyIds = validatedEpicEnemyIds;
	}

	private static List<String> validateEnemyIds(List<String> enemyIds, String groupName) {
		if (enemyIds == null || enemyIds.isEmpty()) {
			throw new IllegalArgumentException(groupName + " enemy IDs cannot be empty.");
		}

		if (enemyIds.size() > EnemyBattleGroup.MAXIMUM_SIZE) {
			throw new IllegalArgumentException(
					groupName + " enemy group cannot contain more than " + EnemyBattleGroup.MAXIMUM_SIZE + " enemies.");
		}

		for (String enemyId : enemyIds) {
			if (enemyId == null || enemyId.isBlank()) {
				throw new IllegalArgumentException(groupName + " enemy ID cannot be empty.");
			}
		}

		if (new HashSet<>(enemyIds).size() != enemyIds.size()) {
			throw new IllegalArgumentException(groupName + " enemy IDs cannot contain duplicates.");
		}

		return List.copyOf(enemyIds);
	}

	public List<String> getBossEnemyIds() {
		return bossEnemyIds;
	}

	public List<String> getEpicEnemyIds() {
		return epicEnemyIds;
	}

	public String getBossEnemyId() {
		return bossEnemyIds.get(0);
	}

	public String getEpicEnemyId() {
		return epicEnemyIds.get(0);
	}
}
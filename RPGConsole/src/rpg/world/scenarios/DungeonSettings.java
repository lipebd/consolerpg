package rpg.world.scenarios;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import rpg.battle.EnemyBattleGroup;
import rpg.world.exploration.ExplorationAreaRestriction;

// Stores the enemy configuration specific to a dungeon.
public final class DungeonSettings {

	private final List<String> bossEnemyIds;
    private final List<String> recurringBossEnemyIds;
    private final int recurringBossCooldownHours;
	private final List<String> epicEnemyIds;
    private final int epicMinPosition;
    private final ExplorationAreaRestriction epicAreaRestriction;
    private final int epicEnemyCooldownHours;

    public DungeonSettings(
            List<String> bossEnemyIds,
            List<String> recurringBossEnemyIds,
            int recurringBossCooldownHours,
            List<String> epicEnemyIds,
            int epicMinPosition,
            ExplorationAreaRestriction epicAreaRestriction,
            int epicEnemyCooldownHours,
            int explorationLength) {
		List<String> validatedBossEnemyIds = validateEnemyIds(bossEnemyIds, "Boss");
        List<String> validatedRecurringBossEnemyIds =
                validateEnemyIds(recurringBossEnemyIds, "Recurring boss");
		List<String> validatedEpicEnemyIds = validateEnemyIds(epicEnemyIds, "Epic");

		if (!Collections.disjoint(validatedBossEnemyIds, validatedEpicEnemyIds)) {
			throw new IllegalArgumentException("Boss and epic enemy IDs must be different.");
		}

        if (!Collections.disjoint(validatedRecurringBossEnemyIds, validatedEpicEnemyIds)) {
            throw new IllegalArgumentException(
                    "Recurring boss and epic enemy IDs must be different.");
        }

        if (recurringBossCooldownHours <= 0) {
            throw new IllegalArgumentException("Recurring boss cooldown hours must be positive.");
        }

        if (epicMinPosition < 1 || epicMinPosition >= explorationLength) {
            throw new IllegalArgumentException(
                    "Epic minimum position must be between 1 and "
                            + (explorationLength - 1)
                            + ": "
                            + epicMinPosition);
        }

        if (epicEnemyCooldownHours <= 0) {
            throw new IllegalArgumentException("Epic enemy cooldown hours must be positive.");
        }

		this.bossEnemyIds = validatedBossEnemyIds;
        this.recurringBossEnemyIds = validatedRecurringBossEnemyIds;
        this.recurringBossCooldownHours = recurringBossCooldownHours;
		this.epicEnemyIds = validatedEpicEnemyIds;
        this.epicMinPosition = epicMinPosition;
        this.epicAreaRestriction =
                Objects.requireNonNull(
                        epicAreaRestriction, "Epic area restriction cannot be null.");
        this.epicEnemyCooldownHours = epicEnemyCooldownHours;
	}

	private static List<String> validateEnemyIds(List<String> enemyIds, String groupName) {
		if (enemyIds == null || enemyIds.isEmpty()) {
			throw new IllegalArgumentException(groupName + " enemy IDs cannot be empty.");
		}

		if (enemyIds.size() > EnemyBattleGroup.MAXIMUM_SIZE) {
			throw new IllegalArgumentException(
                    groupName
                            + " enemy group cannot contain more than "
                            + EnemyBattleGroup.MAXIMUM_SIZE
                            + " enemies.");
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

    public List<String> getRecurringBossEnemyIds() {
        return recurringBossEnemyIds;
    }

    public int getRecurringBossCooldownHours() {
        return recurringBossCooldownHours;
    }

	public List<String> getEpicEnemyIds() {
		return epicEnemyIds;
	}

    public int getEpicMinPosition() {
        return epicMinPosition;
    }

    public ExplorationAreaRestriction getEpicAreaRestriction() {
        return epicAreaRestriction;
    }

    public int getEpicEnemyCooldownHours() {
        return epicEnemyCooldownHours;
    }

	public String getBossEnemyId() {
		return bossEnemyIds.get(0);
	}

    public String getRecurringBossEnemyId() {
        return recurringBossEnemyIds.get(0);
    }

	public String getEpicEnemyId() {
		return epicEnemyIds.get(0);
	}
}
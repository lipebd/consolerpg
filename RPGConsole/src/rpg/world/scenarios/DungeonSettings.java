package rpg.world.scenarios;

// Stores the enemy configuration specific to a dungeon.
public final class DungeonSettings {

    private final String bossEnemyId;
    private final String epicEnemyId;

    public DungeonSettings(
            String bossEnemyId,
            String epicEnemyId
    ) {
        if (bossEnemyId == null || bossEnemyId.isBlank()) {
            throw new IllegalArgumentException(
                    "Boss enemy ID cannot be empty."
            );
        }

        if (epicEnemyId == null || epicEnemyId.isBlank()) {
            throw new IllegalArgumentException(
                    "Epic enemy ID cannot be empty."
            );
        }

        if (bossEnemyId.equals(epicEnemyId)) {
            throw new IllegalArgumentException(
                    "Boss and epic enemy IDs must be different."
            );
        }

        this.bossEnemyId = bossEnemyId;
        this.epicEnemyId = epicEnemyId;
    }

    public String getBossEnemyId() {
        return bossEnemyId;
    }

    public String getEpicEnemyId() {
        return epicEnemyId;
    }
}
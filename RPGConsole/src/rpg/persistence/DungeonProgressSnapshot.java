package rpg.persistence;

// Stores the persistent progression state of one dungeon.
public final class DungeonProgressSnapshot {

    private final String dungeonId;
    private final boolean narrativeBossDefeated;
    private final long recurringBossAvailableAtHour;
    private final long epicAvailableAtHour;

    public DungeonProgressSnapshot(
            String dungeonId,
            boolean narrativeBossDefeated,
            long recurringBossAvailableAtHour,
            long epicAvailableAtHour) {
        if (dungeonId == null || dungeonId.isBlank()) {
            throw new IllegalArgumentException("Dungeon ID cannot be null or blank.");
        }

        if (recurringBossAvailableAtHour < 0) {
            throw new IllegalArgumentException(
                    "Recurring boss availability hour cannot be negative.");
        }

        if (epicAvailableAtHour < 0) {
            throw new IllegalArgumentException("Epic enemy availability hour cannot be negative.");
        }

        if (!narrativeBossDefeated && recurringBossAvailableAtHour != 0) {
            throw new IllegalArgumentException(
                    "Recurring boss availability must be zero before the narrative boss is defeated.");
        }

        if (narrativeBossDefeated && recurringBossAvailableAtHour == 0) {
            throw new IllegalArgumentException(
                    "Recurring boss availability must be defined after the narrative boss is defeated.");
        }

        this.dungeonId = dungeonId;
        this.narrativeBossDefeated = narrativeBossDefeated;
        this.recurringBossAvailableAtHour = recurringBossAvailableAtHour;
        this.epicAvailableAtHour = epicAvailableAtHour;
    }

    public String getDungeonId() {
        return dungeonId;
    }

    public boolean isNarrativeBossDefeated() {
        return narrativeBossDefeated;
    }

    public long getRecurringBossAvailableAtHour() {
        return recurringBossAvailableAtHour;
    }

    public long getEpicAvailableAtHour() {
        return epicAvailableAtHour;
    }
}

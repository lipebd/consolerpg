package rpg.world.scenarios;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Tracks permanent dungeon progression and encounter availability.
public final class DungeonProgress {

    private static final long INITIALLY_AVAILABLE_HOUR = 0;

    private final ScenarioCatalog scenarioCatalog;
    private final Map<String, DungeonState> statesByDungeonId;

    public DungeonProgress(ScenarioCatalog scenarioCatalog) {
        this.scenarioCatalog =
                Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
        this.statesByDungeonId = new LinkedHashMap<>();

        registerDungeons();
    }

    private void registerDungeons() {
        for (Scenario scenario : scenarioCatalog.getAll()) {
            if (scenario instanceof Dungeon dungeon) {
                statesByDungeonId.put(dungeon.getId(), new DungeonState());
            }
        }
    }

    public boolean isNarrativeBossDefeated(String dungeonId) {
        return getState(dungeonId).narrativeBossDefeated;
    }

    public boolean isRecurringBossAvailable(String dungeonId, long elapsedHours) {
        validateElapsedHours(elapsedHours);

        DungeonState state = getState(dungeonId);

        return state.narrativeBossDefeated && elapsedHours >= state.recurringBossAvailableAtHour;
    }

    public boolean isEpicAvailable(String dungeonId, long elapsedHours) {
        validateElapsedHours(elapsedHours);

        return elapsedHours >= getState(dungeonId).epicAvailableAtHour;
    }

    public void recordNarrativeBossVictory(String dungeonId, long elapsedHours) {
        validateElapsedHours(elapsedHours);

        Dungeon dungeon = getDungeon(dungeonId);
        DungeonState state = getState(dungeonId);

        if (state.narrativeBossDefeated) {
            throw new IllegalStateException(
                    "Narrative boss was already defeated for dungeon: " + dungeonId);
        }

        long nextAvailableHour =
                calculateNextAvailableHour(
                        elapsedHours,
                        dungeon.getDungeonSettings().getRecurringBossCooldownHours(),
                        dungeonId,
                        "recurring boss");

        state.narrativeBossDefeated = true;
        state.recurringBossAvailableAtHour = nextAvailableHour;
    }

    public void recordRecurringBossVictory(String dungeonId, long elapsedHours) {
        validateElapsedHours(elapsedHours);

        Dungeon dungeon = getDungeon(dungeonId);
        DungeonState state = getState(dungeonId);

        if (!state.narrativeBossDefeated) {
            throw new IllegalStateException(
                    "Recurring boss victory requires narrative dungeon completion: " + dungeonId);
        }

        if (elapsedHours < state.recurringBossAvailableAtHour) {
            throw new IllegalStateException(
                    "Recurring boss is still in cooldown for dungeon: " + dungeonId);
        }

        state.recurringBossAvailableAtHour =
                calculateNextAvailableHour(
                        elapsedHours,
                        dungeon.getDungeonSettings().getRecurringBossCooldownHours(),
                        dungeonId,
                        "recurring boss");
    }

    public void recordEpicVictory(String dungeonId, long elapsedHours) {
        validateElapsedHours(elapsedHours);

        Dungeon dungeon = getDungeon(dungeonId);
        DungeonState state = getState(dungeonId);

        if (elapsedHours < state.epicAvailableAtHour) {
            throw new IllegalStateException(
                    "Epic encounter is still in cooldown for dungeon: " + dungeonId);
        }

        state.epicAvailableAtHour =
                calculateNextAvailableHour(
                        elapsedHours,
                        dungeon.getDungeonSettings().getEpicEnemyCooldownHours(),
                        dungeonId,
                        "epic encounter");
    }

    
    public List<String> getDungeonIds() {
        return List.copyOf(statesByDungeonId.keySet());
    }

    public long getRecurringBossAvailableAtHour(String dungeonId) {
        return getState(dungeonId).recurringBossAvailableAtHour;
    }

    public long getEpicAvailableAtHour(String dungeonId) {
        return getState(dungeonId).epicAvailableAtHour;
    }

    public void restoreState(
            String dungeonId,
            boolean narrativeBossDefeated,
            long recurringBossAvailableAtHour,
            long epicAvailableAtHour) {
        if (recurringBossAvailableAtHour < 0) {
            throw new IllegalArgumentException(
                    "Recurring boss availability hour cannot be negative.");
        }

        if (epicAvailableAtHour < 0) {
            throw new IllegalArgumentException(
                    "Epic encounter availability hour cannot be negative.");
        }

        if (!narrativeBossDefeated && recurringBossAvailableAtHour != 0) {
            throw new IllegalArgumentException(
                    "Recurring boss availability must be zero before narrative dungeon completion: "
                            + dungeonId);
        }

        if (narrativeBossDefeated && recurringBossAvailableAtHour == 0) {
            throw new IllegalArgumentException(
                    "Recurring boss availability must be defined after narrative dungeon completion: "
                            + dungeonId);
        }

        DungeonState state = getState(dungeonId);

        state.narrativeBossDefeated = narrativeBossDefeated;
        state.recurringBossAvailableAtHour = recurringBossAvailableAtHour;
        state.epicAvailableAtHour = epicAvailableAtHour;
    }

    private Dungeon getDungeon(String dungeonId) {
        Scenario scenario = scenarioCatalog.getById(dungeonId);

        if (!(scenario instanceof Dungeon dungeon)) {
            throw new IllegalArgumentException("Scenario is not a dungeon: " + dungeonId);
        }

        return dungeon;
    }

    private DungeonState getState(String dungeonId) {
        getDungeon(dungeonId);

        DungeonState state = statesByDungeonId.get(dungeonId);

        if (state == null) {
            throw new IllegalArgumentException("Dungeon progress is not registered: " + dungeonId);
        }

        return state;
    }

    private long calculateNextAvailableHour(
            long elapsedHours, int cooldownHours, String dungeonId, String encounterName) {
        try {
            return Math.addExact(elapsedHours, cooldownHours);
        } catch (ArithmeticException exception) {
            throw new IllegalStateException(
                    "Cooldown exceeds the supported world time for "
                            + encounterName
                            + " in dungeon: "
                            + dungeonId,
                    exception);
        }
    }

    private void validateElapsedHours(long elapsedHours) {
        if (elapsedHours < 0) {
            throw new IllegalArgumentException("Elapsed world hours cannot be negative.");
        }
    }

    private static final class DungeonState {

        private boolean narrativeBossDefeated;
        private long recurringBossAvailableAtHour;
        private long epicAvailableAtHour;

        private DungeonState() {
            narrativeBossDefeated = false;
            recurringBossAvailableAtHour = INITIALLY_AVAILABLE_HOUR;
            epicAvailableAtHour = INITIALLY_AVAILABLE_HOUR;
        }
    }
}

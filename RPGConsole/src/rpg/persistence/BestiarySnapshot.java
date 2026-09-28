package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Stores the immutable persisted state of the campaign bestiary.
public final class BestiarySnapshot {

    private final List<String> discoveredEnemyIds;

    public BestiarySnapshot(List<String> discoveredEnemyIds) {
        Objects.requireNonNull(
                discoveredEnemyIds,
                "Discovered enemy IDs cannot be null."
        );

        Set<String> uniqueEnemyIds = new HashSet<>();

        for (String enemyId : discoveredEnemyIds) {
            Objects.requireNonNull(
                    enemyId,
                    "Discovered enemy ID cannot be null."
            );

            if (!uniqueEnemyIds.add(enemyId)) {
                throw new IllegalArgumentException(
                        "Discovered enemy ID is duplicated: "
                                + enemyId
                );
            }
        }

        this.discoveredEnemyIds =
                List.copyOf(discoveredEnemyIds);
    }

    public List<String> getDiscoveredEnemyIds() {
        return discoveredEnemyIds;
    }
}

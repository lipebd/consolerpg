package rpg.world.bestiary;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class BestiaryStateCatalog {

    private final Set<String> discoveredEnemyIds;

    public BestiaryStateCatalog() {
        this.discoveredEnemyIds = new LinkedHashSet<>();
    }

    public BestiaryStateCatalog(List<String> discoveredEnemyIds) {
        Objects.requireNonNull(
                discoveredEnemyIds,
                "Discovered enemy IDs cannot be null."
        );

        this.discoveredEnemyIds = new LinkedHashSet<>();

        for (String enemyId : discoveredEnemyIds) {
            this.discover(enemyId);
        }
    }

    public void discover(String enemyId) {
        Objects.requireNonNull(enemyId, "Enemy ID cannot be null.");

        discoveredEnemyIds.add(enemyId);
    }

    public boolean isDiscovered(String enemyId) {
        Objects.requireNonNull(enemyId, "Enemy ID cannot be null.");

        return discoveredEnemyIds.contains(enemyId);
    }

    public List<String> getDiscoveredEnemyIds() {
        return List.copyOf(discoveredEnemyIds);
    }
}

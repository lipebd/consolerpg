package rpg.enemies;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to all enemy definitions.
public class EnemyCatalog {

    private final Map<String, EnemyDefinition> enemiesById;

    public EnemyCatalog() {
        enemiesById = new LinkedHashMap<>();
    }

    public void register(EnemyDefinition enemyDefinition) {
        String enemyId = enemyDefinition.getId();

        if (enemiesById.containsKey(enemyId)) {
            throw new IllegalArgumentException(
                    "Enemy definition ID is already registered: " + enemyId);
        }

        enemiesById.put(enemyId, enemyDefinition);
    }

    public EnemyDefinition getById(String id) {
        EnemyDefinition enemyDefinition = enemiesById.get(id);

        if (enemyDefinition == null) {
            throw new IllegalArgumentException(
                    "Enemy not found: " + id
            );
        }

        return enemyDefinition;
    }

    public List<EnemyDefinition> getAll() {
        return List.copyOf(enemiesById.values());
    }
}
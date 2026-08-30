package rpg.world.encounters;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDefinition;

// Selects an enemy definition using scenario encounter weights.
public final class EncounterGenerator {

    private final EnemyCatalog enemyCatalog;
    private final EncounterCatalog encounterCatalog;
    private final Random random;

    public EncounterGenerator(
            EnemyCatalog enemyCatalog,
            EncounterCatalog encounterCatalog,
            Random random
    ) {
        this.enemyCatalog = enemyCatalog;
        this.encounterCatalog = encounterCatalog;
        this.random = random;
    }

    public Optional<EnemyDefinition> generate(
            String scenarioId,
            int position,
            boolean specialArea
    ) {
        List<EncounterEntry> availableEntries =
                encounterCatalog.getAvailableEntries(
                        scenarioId,
                        position,
                        specialArea
                );

        if (availableEntries.isEmpty()) {
            return Optional.empty();
        }

        int totalWeight = 0;

        for (EncounterEntry entry : availableEntries) {
            totalWeight += entry.getWeight();
        }

        int roll = random.nextInt(totalWeight);
        int accumulatedWeight = 0;

        for (EncounterEntry entry : availableEntries) {
            accumulatedWeight += entry.getWeight();

            if (roll < accumulatedWeight) {
                EnemyDefinition enemyDefinition =
                        enemyCatalog.getById(
                                entry.getEnemyId()
                        );

                return Optional.of(enemyDefinition);
            }
        }

        throw new IllegalStateException(
                "Encounter selection failed."
        );
    }
}
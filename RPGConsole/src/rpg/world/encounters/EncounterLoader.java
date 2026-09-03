package rpg.world.encounters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.enemies.EnemyCatalog;
import rpg.world.exploration.ExplorationAreaRestriction;

// Loads encounter entries from an external text file.
public final class EncounterLoader {

    public EncounterCatalog load(
            Path filePath,
            EnemyCatalog enemyCatalog
    ) throws IOException {
        EncounterCatalog encounterCatalog =
                new EncounterCatalog();

        List<String> lines =
                Files.readAllLines(filePath);

        for (String line : lines) {
            line = line.trim();

            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            registerEntry(
                    line,
                    enemyCatalog,
                    encounterCatalog
            );
        }

        return encounterCatalog;
    }

    private void registerEntry(
            String line,
            EnemyCatalog enemyCatalog,
            EncounterCatalog encounterCatalog
    ) {
        String[] fields = line.split(";", -1);

        if (fields.length != 7) {
            throw new IllegalArgumentException(
                    "Invalid encounter data: " + line
            );
        }

        String scenarioId = fields[0].trim();
        String enemyId = fields[1].trim();

        int minPosition =
                Integer.parseInt(fields[2].trim());

        int maxPosition =
                Integer.parseInt(fields[3].trim());

        int weight =
                Integer.parseInt(fields[4].trim());

        int maxCopies =
                Integer.parseInt(fields[5].trim());
        
        ExplorationAreaRestriction areaRestriction =
                ExplorationAreaRestriction.valueOf(
                        fields[6].trim().toUpperCase()
                );

        // Verifies that the referenced enemy exists.
        enemyCatalog.getById(enemyId);

        EncounterEntry encounterEntry =
                new EncounterEntry(
                        enemyId,
                        minPosition,
                        maxPosition,
                        weight,
                        maxCopies,
                        areaRestriction
                );

        encounterCatalog.register(
                scenarioId,
                encounterEntry
		);
	}
}
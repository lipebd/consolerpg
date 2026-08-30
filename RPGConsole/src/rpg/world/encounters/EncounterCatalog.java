package rpg.world.encounters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores encounter entries grouped by scenario ID.
public final class EncounterCatalog {

    private final Map<String, List<EncounterEntry>> entriesByScenario;

    public EncounterCatalog() {
        entriesByScenario = new LinkedHashMap<>();
    }

    public void register(
            String scenarioId,
            EncounterEntry encounterEntry
    ) {
        List<EncounterEntry> scenarioEntries =
                entriesByScenario.computeIfAbsent(
                        scenarioId,
                        id -> new ArrayList<>()
                );

        scenarioEntries.add(encounterEntry);
    }

    public List<EncounterEntry> getAvailableEntries(
            String scenarioId,
            int position,
            boolean specialArea
    ) {
        List<EncounterEntry> scenarioEntries =
                entriesByScenario.getOrDefault(
                        scenarioId,
                        List.of()
                );

        return scenarioEntries.stream()
                .filter(entry ->
                        entry.isAvailableAt(
                                position,
                                specialArea
                        )
                )
                .toList();
    }
}
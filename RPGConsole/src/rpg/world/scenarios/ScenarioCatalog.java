package rpg.world.scenarios;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to all game scenarios.
public final class ScenarioCatalog {

    private final Map<String, Scenario> scenariosById;

    public ScenarioCatalog() {
        scenariosById = new LinkedHashMap<>();
    }

    public void register(Scenario scenario) {
        String scenarioId = scenario.getId();

        if (scenariosById.containsKey(scenarioId)) {
            throw new IllegalArgumentException(
                    "Scenario ID is already registered: "
                    + scenarioId
            );
        }

        scenariosById.put(
                scenarioId,
                scenario
        );
    }

    public Scenario getById(String id) {
        Scenario scenario = scenariosById.get(id);

        if (scenario == null) {
            throw new IllegalArgumentException(
                    "Scenario not found: " + id
            );
        }

        return scenario;
    }

    public List<Scenario> getAll() {
        return List.copyOf(
                scenariosById.values()
        );
    }
}
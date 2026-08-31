package rpg.world.travel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores the travel destinations available from each scenario.
public final class TravelCatalog {

    private final Map<String, List<String>>
            destinationIdsByScenarioId;

    public TravelCatalog() {
        destinationIdsByScenarioId =
                new LinkedHashMap<>();
    }

    public void register(
            String sourceScenarioId,
            String destinationScenarioId
    ) {
        if (
                sourceScenarioId == null
                || sourceScenarioId.isBlank()
                || destinationScenarioId == null
                || destinationScenarioId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Travel scenario IDs cannot be empty."
            );
        }

        if (sourceScenarioId.equals(destinationScenarioId)) {
            throw new IllegalArgumentException(
                    "A scenario cannot travel to itself."
            );
        }

        List<String> destinationIds =
                destinationIdsByScenarioId.computeIfAbsent(
                        sourceScenarioId,
                        id -> new ArrayList<>()
                );

        if (destinationIds.contains(destinationScenarioId)) {
            throw new IllegalArgumentException(
                    "Travel destination is already registered."
            );
        }

        destinationIds.add(destinationScenarioId);
    }

    public List<String> getDestinationIds(
            String sourceScenarioId
    ) {
        List<String> destinationIds =
                destinationIdsByScenarioId.getOrDefault(
                        sourceScenarioId,
                        List.of()
                );

        return List.copyOf(destinationIds);
    }
}
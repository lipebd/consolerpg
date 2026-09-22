package rpg.narrative;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores narrative locations in stable definition order.
public final class NarrativeLocationCatalog {

    private final Map<String, NarrativeLocationDefinition> locationsById;
    private final Map<String, List<NarrativeLocationDefinition>> locationsByCityId;

    public NarrativeLocationCatalog() {
        locationsById = new LinkedHashMap<>();
        locationsByCityId = new LinkedHashMap<>();
    }

    public void register(NarrativeLocationDefinition locationDefinition) {
        if (locationDefinition == null) {
            throw new IllegalArgumentException("Narrative location definition cannot be null.");
        }

        String locationId = locationDefinition.getId();

        if (locationsById.containsKey(locationId)) {
            throw new IllegalArgumentException(
                    "Narrative location ID is already registered: " + locationId);
        }

        locationsById.put(locationId, locationDefinition);

        locationsByCityId
                .computeIfAbsent(locationDefinition.getCityId(), ignored -> new ArrayList<>())
                .add(locationDefinition);
    }

    public NarrativeLocationDefinition getById(String locationId) {
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException("Narrative location ID cannot be empty.");
        }

        NarrativeLocationDefinition locationDefinition = locationsById.get(locationId);

        if (locationDefinition == null) {
            throw new IllegalArgumentException("Narrative location not found: " + locationId);
        }

        return locationDefinition;
    }

    public List<NarrativeLocationDefinition> getByCityId(String cityId) {
        if (cityId == null || cityId.isBlank()) {
            throw new IllegalArgumentException("City ID cannot be empty.");
        }

        return List.copyOf(locationsByCityId.getOrDefault(cityId, List.of()));
    }

    public List<NarrativeLocationDefinition> getAll() {
        return List.copyOf(locationsById.values());
    }
}

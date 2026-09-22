package rpg.narrative;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Stores fixed NPC placements in stable definition order.
public final class NpcPlacementCatalog {

    private final Map<String, NpcPlacementDefinition> placementsByNpcId;
    private final Map<String, List<NpcPlacementDefinition>> placementsByLocationId;

    public NpcPlacementCatalog() {
        placementsByNpcId = new LinkedHashMap<>();
        placementsByLocationId = new LinkedHashMap<>();
    }

    public void register(NpcPlacementDefinition placementDefinition) {
        if (placementDefinition == null) {
            throw new IllegalArgumentException("NPC placement definition cannot be null.");
        }

        String npcId = placementDefinition.getNpcId();

        if (placementsByNpcId.containsKey(npcId)) {
            throw new IllegalArgumentException(
                    "NPC already has a narrative-location placement: " + npcId);
        }

        placementsByNpcId.put(npcId, placementDefinition);

        placementsByLocationId
                .computeIfAbsent(
                        placementDefinition.getNarrativeLocationId(), ignored -> new ArrayList<>())
                .add(placementDefinition);
    }

    public Optional<NpcPlacementDefinition> getByNpcId(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            throw new IllegalArgumentException("NPC ID cannot be empty.");
        }

        return Optional.ofNullable(placementsByNpcId.get(npcId));
    }

    public List<NpcPlacementDefinition> getByLocationId(String narrativeLocationId) {
        if (narrativeLocationId == null || narrativeLocationId.isBlank()) {
            throw new IllegalArgumentException("Narrative location ID cannot be empty.");
        }

        return List.copyOf(placementsByLocationId.getOrDefault(narrativeLocationId, List.of()));
    }

    public List<NpcPlacementDefinition> getAll() {
        return List.copyOf(placementsByNpcId.values());
    }
}

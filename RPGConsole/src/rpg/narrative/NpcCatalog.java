package rpg.narrative;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to ordinary NPC definitions.
public final class NpcCatalog {

    private final Map<String, NpcDefinition> npcsById;

    public NpcCatalog() {
        npcsById = new LinkedHashMap<>();
    }

    public void register(NpcDefinition npcDefinition) {
        if (npcDefinition == null) {
            throw new IllegalArgumentException("NPC definition cannot be null.");
        }

        String npcId = npcDefinition.getId();

        if (npcsById.containsKey(npcId)) {
            throw new IllegalArgumentException("NPC ID is already registered: " + npcId);
        }

        npcsById.put(npcId, npcDefinition);
    }

    public NpcDefinition getById(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            throw new IllegalArgumentException("NPC ID cannot be empty.");
        }

        NpcDefinition npcDefinition = npcsById.get(npcId);

        if (npcDefinition == null) {
            throw new IllegalArgumentException("NPC not found: " + npcId);
        }

        return npcDefinition;
    }

    public List<NpcDefinition> getAll() {
        return List.copyOf(npcsById.values());
    }
}

package rpg.flags;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to external permanent-flag definitions.
public final class FlagCatalog {

    private final Map<String, FlagDefinition> flagsById;

    public FlagCatalog() {
        flagsById = new LinkedHashMap<>();
    }

    public void register(FlagDefinition flagDefinition) {
        if (flagDefinition == null) {
            throw new IllegalArgumentException("Flag definition cannot be null.");
        }

        String flagId = flagDefinition.getId();

        if (flagsById.containsKey(flagId)) {
            throw new IllegalArgumentException("Flag ID is already registered: " + flagId);
        }

        flagsById.put(flagId, flagDefinition);
    }

    public FlagDefinition getById(String flagId) {
        if (flagId == null || flagId.isBlank()) {
            throw new IllegalArgumentException("Flag ID cannot be empty.");
        }

        FlagDefinition flagDefinition = flagsById.get(flagId);

        if (flagDefinition == null) {
            throw new IllegalArgumentException("Flag not found: " + flagId);
        }

        return flagDefinition;
    }

    public List<FlagDefinition> getAll() {
        return List.copyOf(flagsById.values());
    }
}

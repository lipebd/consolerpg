package rpg.requirements;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to requirement-set definitions.
public final class RequirementCatalog {

    private final Map<String, RequirementSetDefinition> definitionsById;

    public RequirementCatalog() {
        definitionsById = new LinkedHashMap<>();
    }

    public void register(RequirementSetDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Requirement set definition cannot be null.");
        }

        String setId = definition.getId();

        if (definitionsById.containsKey(setId)) {
            throw new IllegalArgumentException(
                    "Requirement set ID is already registered: " + setId);
        }

        definitionsById.put(setId, definition);
    }

    public RequirementSetDefinition getById(String setId) {
        if (setId == null || setId.isBlank()) {
            throw new IllegalArgumentException("Requirement set ID cannot be empty.");
        }

        RequirementSetDefinition definition = definitionsById.get(setId);

        if (definition == null) {
            throw new IllegalArgumentException("Requirement set not found: " + setId);
        }

        return definition;
    }

    public List<RequirementSetDefinition> getAll() {
        return List.copyOf(definitionsById.values());
    }
}

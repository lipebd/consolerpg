package rpg.items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores recovery item definitions by item ID.
public final class RecoveryItemDefinitionCatalog {

        private final Map<String, RecoveryItemDefinition> definitionsByItemId;

        public RecoveryItemDefinitionCatalog() {
                definitionsByItemId = new LinkedHashMap<>();
        }

        public void register(RecoveryItemDefinition definition) {
                if (definition == null) {
                        throw new IllegalArgumentException(
                                        "Recovery item definition cannot be null.");
                }

                String itemId = definition.getItemId();

                if (definitionsByItemId.containsKey(itemId)) {
                        throw new IllegalArgumentException(
                                        "Recovery item definition is already registered: "
                                                        + itemId);
                }

                definitionsByItemId.put(itemId, definition);
        }

        public RecoveryItemDefinition getByItemId(String itemId) {
                if (itemId == null || itemId.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Recovery item ID cannot be empty.");
                }

                RecoveryItemDefinition definition =
                                definitionsByItemId.get(itemId);

                if (definition == null) {
                        throw new IllegalArgumentException(
                                        "Recovery item definition not found: "
                                                        + itemId);
                }

                return definition;
        }

        public boolean containsItemId(String itemId) {
                return definitionsByItemId.containsKey(itemId);
        }

        public List<RecoveryItemDefinition> getAll() {
                return List.copyOf(definitionsByItemId.values());
        }
}
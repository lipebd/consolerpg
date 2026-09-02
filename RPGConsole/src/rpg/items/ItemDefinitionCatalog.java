package rpg.items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to item definitions.
public final class ItemDefinitionCatalog {

        private final Map<String, ItemDefinition> definitionsById;

        public ItemDefinitionCatalog() {
                definitionsById = new LinkedHashMap<>();
        }

        public void register(ItemDefinition definition) {
                if (definition == null) {
                        throw new IllegalArgumentException(
                                        "Item definition cannot be null.");
                }

                String itemId = definition.getId();

                if (definitionsById.containsKey(itemId)) {
                        throw new IllegalArgumentException(
                                        "Item definition ID is already registered: "
                                                        + itemId);
                }

                definitionsById.put(itemId, definition);
        }

        public ItemDefinition getById(String itemId) {
                if (itemId == null || itemId.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Item definition ID cannot be empty.");
                }

                ItemDefinition definition = definitionsById.get(itemId);

                if (definition == null) {
                        throw new IllegalArgumentException(
                                        "Item definition not found: " + itemId);
                }

                return definition;
        }

        public List<ItemDefinition> getAll() {
                return List.copyOf(definitionsById.values());
        }
}
package rpg.items;

import java.util.Locale;
import java.util.Map;

// Stores the shared definitions of all game items.
public final class ItemCatalog {

    public static final HealingItem MERUMA =
            new HealingItem(
                    "meruma",
                    "Merumã",
                    "Uma fruta que recupera 25 pontos de HP.",
                    25
            );

    private static final Map<String, Item> ITEMS_BY_ID =
            Map.of(
                    MERUMA.getId(),
                    MERUMA
            );

    private ItemCatalog() {
    }

    public static Item getById(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException(
                    "Item ID cannot be empty."
            );
        }

        String normalizedId =
                itemId.trim().toLowerCase(Locale.ROOT);

        Item item = ITEMS_BY_ID.get(normalizedId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Unknown item ID: " + itemId
            );
        }

        return item;
    }
}
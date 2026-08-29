package rpg.items;

// Stores the shared definitions of all game items.
public final class ItemCatalog {

    public static final HealingItem MERUMA =
            new HealingItem(
                    "meruma",
                    "Merumã",
                    "Uma fruta que recupera 25 pontos de HP.",
                    25
            );

    private ItemCatalog() {
    }
}
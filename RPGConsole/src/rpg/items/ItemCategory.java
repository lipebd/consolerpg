package rpg.items;

// Defines the main inventory category of an item.
public enum ItemCategory {

        CONSUMABLE("Consumível"),
        WEAPON("Arma"),
        ARMOR("Armadura"),
        TRINKET("Berloque"),
        KEY_ITEM("Item-chave"),
        MATERIAL("Material"),
        SKILL_SCROLL("Pergaminho de habilidade");

        private final String displayName;

        ItemCategory(String displayName) {
                this.displayName = displayName;
        }

        public String getDisplayName() {
                return displayName;
        }

        @Override
        public String toString() {
                return displayName;
        }
}
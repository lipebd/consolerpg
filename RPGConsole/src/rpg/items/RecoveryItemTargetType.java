package rpg.items;

// Defines which party members can be affected by a recovery item.
public enum RecoveryItemTargetType {

        SINGLE_ALLY("Um aliado"),
        ALL_ALLIES("Todos os aliados");

        private final String displayName;

        RecoveryItemTargetType(String displayName) {
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
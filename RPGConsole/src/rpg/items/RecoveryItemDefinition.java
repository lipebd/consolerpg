package rpg.items;

// Stores the immutable specialized data of a recovery item.
public final class RecoveryItemDefinition {

        private final String itemId;
        private final RecoveryItemTargetType targetType;
        private final int hpRecovery;
        private final int mpRecovery;
        private final int spRecovery;
        private final int reviveHpRecovery;
        private final boolean purifiesStatusEffects;

        public RecoveryItemDefinition(
                        String itemId,
                        RecoveryItemTargetType targetType,
                        int hpRecovery,
                        int mpRecovery,
                        int spRecovery,
                        int reviveHpRecovery,
                        boolean purifiesStatusEffects) {
                if (itemId == null || itemId.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Recovery item ID cannot be empty.");
                }

                if (targetType == null) {
                        throw new IllegalArgumentException(
                                        "Recovery item target type cannot be null.");
                }

                if (hpRecovery < 0
                                || mpRecovery < 0
                                || spRecovery < 0
                                || reviveHpRecovery < 0) {
                        throw new IllegalArgumentException(
                                        "Recovery item values cannot be negative.");
                }

                if (hpRecovery == 0
                                && mpRecovery == 0
                                && spRecovery == 0
                                && reviveHpRecovery == 0
                                && !purifiesStatusEffects) {
                        throw new IllegalArgumentException(
                                        "Recovery item must have at least one effective component.");
                }

                this.itemId = itemId;
                this.targetType = targetType;
                this.hpRecovery = hpRecovery;
                this.mpRecovery = mpRecovery;
                this.spRecovery = spRecovery;
                this.reviveHpRecovery = reviveHpRecovery;
                this.purifiesStatusEffects = purifiesStatusEffects;
        }

        public String getItemId() {
                return itemId;
        }

        public RecoveryItemTargetType getTargetType() {
                return targetType;
        }

        public int getHpRecovery() {
                return hpRecovery;
        }

        public int getMpRecovery() {
                return mpRecovery;
        }

        public int getSpRecovery() {
                return spRecovery;
        }

        public int getReviveHpRecovery() {
                return reviveHpRecovery;
        }

        public boolean purifiesStatusEffects() {
                return purifiesStatusEffects;
        }
}
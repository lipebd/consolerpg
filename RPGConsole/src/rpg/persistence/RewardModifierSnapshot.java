package rpg.persistence;

// Stores one immutable reward-modifier state in a campaign snapshot.
public final class RewardModifierSnapshot {

        private final int modifierPercent;
        private final int victoriesRemaining;

        public RewardModifierSnapshot(int modifierPercent, int victoriesRemaining) {
                if (modifierPercent < 0) {
                        throw new IllegalArgumentException("Reward modifier percentage cannot be negative.");
                }

                if (victoriesRemaining < 0) {
                        throw new IllegalArgumentException("Reward modifier duration cannot be negative.");
                }

                boolean modifierIsInactive = modifierPercent == 0;
                boolean durationIsInactive = victoriesRemaining == 0;

                if (modifierIsInactive != durationIsInactive) {
                        throw new IllegalArgumentException(
                                        "Reward modifier percentage and duration must both be zero or both be positive."
                        );
                }

                this.modifierPercent = modifierPercent;
                this.victoriesRemaining = victoriesRemaining;
        }

        public int getModifierPercent() {
                return modifierPercent;
        }

        public int getVictoriesRemaining() {
                return victoriesRemaining;
        }

        public boolean isActive() {
                return victoriesRemaining > 0;
        }
}
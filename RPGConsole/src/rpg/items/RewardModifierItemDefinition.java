package rpg.items;

// Stores the immutable reward bonuses granted by one item.
public record RewardModifierItemDefinition(
        String itemId,
        int experiencePercent,
        int goldPercent,
        int rewardedVictories
) {
    public RewardModifierItemDefinition {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException(
                    "Reward modifier item ID cannot be empty.");
        }

        if (experiencePercent < 0 || goldPercent < 0
                || (experiencePercent == 0 && goldPercent == 0)) {
            throw new IllegalArgumentException(
                    "At least one reward modifier must be positive.");
        }

        if (rewardedVictories < 1) {
            throw new IllegalArgumentException(
                    "Rewarded victories must be positive.");
        }
    }
}
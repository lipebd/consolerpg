package rpg.world.encounters;

import java.util.Objects;

import rpg.world.exploration.ExplorationAreaRestriction;

// Associates an enemy with its valid exploration range and area restriction.
public final class EncounterEntry {

    private final String enemyId;
    private final int minPosition;
    private final int maxPosition;
    private final int weight;
    private final int maxCopies;
    private final ExplorationAreaRestriction areaRestriction;

    public EncounterEntry(
            String enemyId,
            int minPosition,
            int maxPosition,
            int weight,
            int maxCopies,
            ExplorationAreaRestriction areaRestriction
    ) {
        if (enemyId == null || enemyId.isBlank()) {
            throw new IllegalArgumentException(
                    "Enemy ID cannot be empty."
            );
        }

        if (minPosition < 1) {
            throw new IllegalArgumentException(
                    "Minimum encounter position must be at least one."
            );
        }

        if (maxPosition < minPosition) {
            throw new IllegalArgumentException(
                    "Maximum encounter position cannot be lower than the minimum position."
            );
        }

        if (weight <= 0) {
            throw new IllegalArgumentException(
                    "Encounter weight must be greater than zero."
            );
        }
        if (maxCopies <= 0) {
            throw new IllegalArgumentException(
                    "Maximum enemy copies must be greater than zero."
            );
        }

        this.enemyId = enemyId;
        this.minPosition = minPosition;
        this.maxPosition = maxPosition;
        this.weight = weight;
        this.maxCopies = maxCopies;
        this.areaRestriction = Objects.requireNonNull(
                areaRestriction,
                "Area restriction cannot be null."
        );
    }

    public boolean isAvailableAt(
            int position,
            boolean specialArea
    ) {
        boolean validPosition =
                position >= minPosition
                && position <= maxPosition;

        return validPosition
                && areaRestriction.allows(specialArea);
    }

    public String getEnemyId() {
        return enemyId;
    }

    public int getMinPosition() {
        return minPosition;
    }

    public int getMaxPosition() {
        return maxPosition;
    }

    public int getWeight() {
        return weight;
    }
    
    public int getMaxCopies() {
        return maxCopies;
    }

    public ExplorationAreaRestriction getAreaRestriction() {
        return areaRestriction;
    }
}